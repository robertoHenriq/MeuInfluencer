(() => {
    "use strict";

    const TAXA_PLATAFORMA = 0.03;
    const state = {
        role: document.body.dataset.role,
        tab: document.body.dataset.defaultTab,
        projects: [...(window.HALL_INITIAL_DATA?.projects ?? [])],
        influencers: [...(window.HALL_INITIAL_DATA?.influencers ?? [])],
        proposals: [...(window.HALL_INITIAL_DATA?.proposals ?? [])],
        ownProjectIds: new Set(),
        compared: new Set(),
        liveActivity: null,
        activityTimer: null
    };

    const $ = (selector, scope = document) => scope.querySelector(selector);
    const $$ = (selector, scope = document) => [...scope.querySelectorAll(selector)];
    const brl = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });
    const number = new Intl.NumberFormat("pt-BR");
    const theme = localStorage.getItem("theme");
    if (theme === "light") document.documentElement.dataset.theme = "light";

    function escapeHtml(value = "") {
        return String(value).replace(/[&<>"']/g, character => ({
            "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
        })[character]);
    }

    function safeUrl(value) {
        try {
            const url = new URL(value, window.location.origin);
            return ["http:", "https:"].includes(url.protocol) ? url.href : "";
        } catch {
            return "";
        }
    }

    function showToast(message, type = "success") {
        const toast = document.createElement("div");
        toast.className = `hall-toast ${type === "error" ? "error" : ""}`;
        toast.innerHTML = `<i class="fa-solid ${type === "error" ? "fa-circle-exclamation" : "fa-circle-check"}" aria-hidden="true"></i><span>${escapeHtml(message)}</span>`;
        $("#toast-region").append(toast);
        window.setTimeout(() => {
            toast.classList.add("is-leaving");
            window.setTimeout(() => toast.remove(), 220);
        }, 3900);
    }

    async function request(url, options = {}) {
        const response = await fetch(url, {
            ...options,
            credentials: "same-origin",
            headers: { "Content-Type": "application/json", ...options.headers }
        });
        const contentType = response.headers.get("content-type") || "";
        const body = contentType.includes("application/json") ? await response.json() : null;
        if (!response.ok) {
            throw new Error(body?.detail || body?.message || `A solicitação falhou (${response.status}).`);
        }
        return body;
    }

    function animateCounters() {
        $$(".count-up").forEach(counter => {
            const target = Number(counter.dataset.count || 0);
            const start = performance.now();
            const duration = 850;
            function tick(now) {
                const progress = Math.min((now - start) / duration, 1);
                const eased = 1 - Math.pow(1 - progress, 3);
                counter.textContent = number.format(Math.round(target * eased));
                if (progress < 1) requestAnimationFrame(tick);
            }
            requestAnimationFrame(tick);
        });
    }

    function updateTabCounts() {
        const projectCount = $("[data-counter='projects']");
        const influencerCount = $("[data-counter='influencers']");
        if (projectCount) projectCount.textContent = number.format(state.projects.length);
        if (influencerCount) influencerCount.textContent = number.format(state.influencers.length);
        $("#received-proposals-count")?.replaceChildren(document.createTextNode(number.format(state.proposals.length)));
    }

    function favoriteIds() {
        return new Set(JSON.parse(localStorage.getItem("hall-favorites") || "[]"));
    }

    function isFavorite(type, id) {
        return favoriteIds().has(`${type}:${id}`);
    }

    function setFavorite(type, id, enabled) {
        const ids = favoriteIds();
        const key = `${type}:${id}`;
        if (enabled) ids.add(key);
        else ids.delete(key);
        localStorage.setItem("hall-favorites", JSON.stringify([...ids]));
    }

    function projectCard(project, index) {
        const favorite = isFavorite("projeto", project.id);
        const isInfluencer = state.role === "INFLUENCER";
        const budget = project.budgetMin && project.budgetMax
            ? `${brl.format(project.budgetMin)} – ${brl.format(project.budgetMax)}`
            : brl.format(project.budget || 0);
        const isNew = project.isNew === true || Number(project.createdMinutes) < 15;
        const button = isInfluencer
            ? `<button class="btn-primary-hall" data-action="send-proposal" data-id="${escapeHtml(project.id)}"><i class="fa-solid fa-paper-plane" aria-hidden="true"></i> Enviar proposta</button>`
            : `<button class="btn-outline-hall" data-action="project-details" data-id="${escapeHtml(project.id)}">Ver detalhes</button>`;
        return `<article class="project-card" style="animation-delay:${Math.min(index * 45, 270)}ms">
            <div class="project-card-top">
                <span class="d-flex align-items-center gap-2"><span class="category-pill">${escapeHtml(project.category)}</span><span class="network-pill">${escapeHtml(project.network)}</span></span>
                <div class="d-flex align-items-center gap-2">${isNew ? '<span class="new-badge" data-new-badge>Novo</span>' : ""}
                    <button class="favorite-button ${favorite ? "is-favorite" : ""}" type="button" data-action="favorite" data-type="projeto" data-id="${escapeHtml(project.id)}" aria-label="${favorite ? "Remover projeto dos favoritos" : "Favoritar projeto"}" aria-pressed="${favorite}"><i class="fa-${favorite ? "solid" : "regular"} fa-heart" aria-hidden="true"></i></button>
                </div>
            </div>
            <h4>${escapeHtml(project.title)}</h4><p class="company-line"><i class="fa-solid fa-store" aria-hidden="true"></i>${escapeHtml(project.company)}</p>
            <p class="project-description">${escapeHtml(project.description)}</p>
            <div class="project-meta"><span><span><i class="fa-solid fa-coins" aria-hidden="true"></i> Orçamento</span><b>${budget}</b></span><span><span><i class="fa-regular fa-clock" aria-hidden="true"></i> Prazo</span><b>${escapeHtml(project.deadline)} dias</b></span></div>
            <div class="project-card-bottom"><div class="card-footnote"><span><i class="fa-regular fa-comment-dots" aria-hidden="true"></i>${number.format(project.proposalCount || 0)} propostas</span><span><i class="fa-regular fa-clock" aria-hidden="true"></i>${escapeHtml(project.createdMinutes === 0 ? "agora" : `${project.createdMinutes} min`)}</span><span class="live-viewers"><i class="fa-solid fa-circle" aria-hidden="true"></i>${escapeHtml(project.views || 2)} vendo</span></div></div>
            <div class="d-flex align-items-center justify-content-between gap-2 mt-2"><span class="secure-badge"><i class="fa-solid fa-shield-halved" aria-hidden="true"></i>Pagamento protegido</span>${button}</div>
        </article>`;
    }

    function influencerCard(influencer, index) {
        const favorite = isFavorite("influenciador", influencer.id);
        const avatar = safeUrl(influencer.avatar);
        const action = state.role === "EMPRESA"
            ? `<button class="btn-primary-hall" data-action="invite-influencer" data-id="${escapeHtml(influencer.id)}"><i class="fa-solid fa-paper-plane" aria-hidden="true"></i> Convidar</button>`
            : `<button class="btn-primary-hall" data-action="influencer-details" data-id="${escapeHtml(influencer.id)}">Ver perfil</button>`;
        return `<article class="influencer-card" style="animation-delay:${Math.min(index * 45, 270)}ms">
            <div class="influencer-cover"></div>
            <div class="influencer-top">
                ${avatar ? `<img class="influencer-avatar" src="${escapeHtml(avatar)}" alt="Foto de ${escapeHtml(influencer.name)}" loading="lazy">` : '<span class="influencer-avatar d-grid place-items-center"><i class="fa-regular fa-user"></i></span>'}
                <div class="d-flex align-items-center gap-2">${influencer.isNew ? '<span class="new-badge" data-new-badge>Novo</span>' : ""}
                    <button class="favorite-button ${favorite ? "is-favorite" : ""}" type="button" data-action="favorite" data-type="influenciador" data-id="${escapeHtml(influencer.id)}" aria-label="${favorite ? "Remover influenciador dos favoritos" : "Favoritar influenciador"}" aria-pressed="${favorite}"><i class="fa-${favorite ? "solid" : "regular"} fa-heart" aria-hidden="true"></i></button>
                </div>
            </div>
            <div class="influencer-body">
                <div class="influencer-name"><div><h4>${escapeHtml(influencer.name)}</h4><p class="influencer-handle">${escapeHtml(influencer.handle)}</p></div><span class="influencer-rating"><i class="fa-solid fa-star" aria-hidden="true"></i> ${Number(influencer.rating).toFixed(1)}</span></div>
                <p class="influencer-bio">${escapeHtml(influencer.bio)}</p>
                <div class="influencer-tags"><span class="influencer-tag">${escapeHtml(influencer.niche)}</span><span class="influencer-tag">${escapeHtml(influencer.neighborhood)}</span><span class="network-pill">${escapeHtml(influencer.network)}</span></div>
                <div class="influencer-stats"><span>Seguidores<strong>${number.format(influencer.followers)}</strong></span><span>Local<strong>${escapeHtml(influencer.neighborhood)} · ${escapeHtml(influencer.city || "São José do Rio Preto")}</strong></span><span>A partir de<strong>${brl.format(influencer.price)}</strong></span></div>
                <div class="influencer-actions">${action}<button class="btn-outline-hall" data-action="influencer-details" data-id="${escapeHtml(influencer.id)}">Ver perfil</button></div>
                <div class="live-viewers mt-3"><i class="fa-solid fa-circle" aria-hidden="true"></i>${escapeHtml(influencer.views || 3)} pessoas vendo agora</div>
            </div>
        </article>`;
    }

    function renderProjects(projects = state.projects) {
        const list = $("#project-list");
        $("#project-result-count").textContent = `${projects.length} ${projects.length === 1 ? "projeto" : "projetos"}`;
        const activity = state.liveActivity?.type === "projeto" ? activityMarkup() : "";
        list.innerHTML = activity || projects.length
            ? `${activity}${projects.map(projectCard).join("")}`
            : emptyMarkup("Nenhum projeto encontrado", "Tente mudar a categoria, a faixa de orçamento ou os filtros da busca.", "fa-magnifying-glass");
        scheduleNewBadges();
    }

    function renderInfluencers(influencers = state.influencers) {
        const list = $("#influencer-list");
        $("#influencer-result-count").textContent = `${influencers.length} ${influencers.length === 1 ? "perfil" : "perfis"}`;
        const activity = state.liveActivity?.type === "influenciador" ? activityMarkup() : "";
        list.innerHTML = activity || influencers.length
            ? `${activity}${influencers.map(influencerCard).join("")}`
            : emptyMarkup("Nenhum perfil encontrado", "Experimente outros filtros ou volte em breve para conhecer novos criadores.", "fa-users");
        scheduleNewBadges();
    }

    function renderProposals(proposals = state.proposals) {
        const list = $("#proposal-list");
        updateTabCounts();
        const activity = state.liveActivity?.type === "proposta" ? activityMarkup() : "";
        if (!proposals.length) {
            list.innerHTML = `${activity}${emptyMarkup("Ainda não há propostas recebidas", "Quando influenciadores enviarem propostas para seus projetos, você poderá comparar e responder por aqui.", "fa-inbox")}`;
            return;
        }
        list.innerHTML = `${activity}${proposals.map(proposal => {
            const avatar = safeUrl(proposal.avatar);
            const statusClass = proposal.status === "ACEITA" ? "accepted" : proposal.status === "RECUSADA" ? "rejected" : "";
            const statusLabel = proposal.status === "ACEITA" ? "Aceita" : proposal.status === "RECUSADA" ? "Recusada" : "Pendente";
            return `<article class="proposal-card" data-proposal-id="${escapeHtml(proposal.id)}">
                <div class="proposal-person">${avatar ? `<img src="${escapeHtml(avatar)}" alt="" loading="lazy">` : '<span class="influencer-avatar"><i class="fa-regular fa-user"></i></span>'}<div><strong>${escapeHtml(proposal.influencer)}</strong><small>${escapeHtml(proposal.handle || "")} · ${escapeHtml(proposal.createdAt || "agora")}</small></div></div>
                <div class="proposal-details"><strong>${escapeHtml(proposal.projectTitle)}</strong><span class="proposal-message">${escapeHtml(proposal.message)}</span><small>${escapeHtml(proposal.deadline)} dias para entrega</small></div>
                <div class="proposal-price"><strong>${brl.format(proposal.amount)}</strong><small>Proposta total</small><span class="status-badge ${statusClass}">${statusLabel}</span></div>
                <div class="proposal-actions">
                    <label class="proposal-check"><input type="checkbox" data-action="compare" value="${escapeHtml(proposal.id)}" ${state.compared.has(proposal.id) ? "checked" : ""} aria-label="Selecionar proposta para comparar"> Comparar</label>
                    ${proposal.status === "PENDENTE" ? `<button class="btn-primary-hall" data-action="accept-proposal" data-id="${escapeHtml(proposal.id)}">Aceitar</button><button class="btn-outline-hall" data-action="reject-proposal" data-id="${escapeHtml(proposal.id)}">Recusar</button>` : ""}
                    <button class="btn-outline-hall" data-action="message-proposal" data-id="${escapeHtml(proposal.id)}">Conversar</button>
                </div>
            </article>`;
        }).join("")}`;
    }

    function emptyMarkup(title, message, icon) {
        return `<div class="empty-state"><i class="fa-solid ${icon}" aria-hidden="true"></i><h4>${title}</h4><p>${message}</p></div>`;
    }

    function activityMarkup() {
        if (!state.liveActivity) return "";
        return `<div class="activity-card"><i class="fa-solid fa-wand-magic-sparkles" aria-hidden="true"></i><div><strong>${escapeHtml(state.liveActivity.title)}</strong><span class="d-block">${escapeHtml(state.liveActivity.detail)}</span></div><span class="ms-auto">agora</span></div>`;
    }

    function scheduleNewBadges() {
        $$("[data-new-badge]").forEach(badge => {
            window.setTimeout(() => badge.remove(), 7000);
        });
    }

    function setLoading(isLoading) {
        $("#hall-skeleton").classList.toggle("is-visible", isLoading);
        $("#hall-skeleton").setAttribute("aria-hidden", String(!isLoading));
    }

    function activeFilterParams() {
        const params = new URLSearchParams();
        const search = $("#hall-search").value.trim();
        if (search) params.set("q", search);
        const read = key => $(`#${key}-mobile`)?.value || $(`#${key}`)?.value || "";
        if (read("filter-category")) params.set("categoria", read("filter-category"));
        if (read("filter-network")) params.set("rede", read("filter-network"));
        if (read("filter-budget")) params.set("orcamentoMax", read("filter-budget"));
        if (read("filter-neighborhood")) params.set("bairro", read("filter-neighborhood"));
        if (read("filter-deadline")) params.set("prazo", read("filter-deadline"));
        if (read("filter-sort")) params.set("ordenacao", read("filter-sort"));
        const followers = read("filter-followers");
        if (followers) {
            const [min, max] = followers.split("-");
            params.set("seguidoresMin", min);
            params.set("seguidoresMax", max);
        }
        return params;
    }

    function countFilters() {
        const keys = ["filter-category", "filter-network", "filter-budget", "filter-followers", "filter-neighborhood", "filter-deadline"];
        let count = 0;
        keys.forEach(key => {
            const value = $(`#${key}-mobile`)?.value || $(`#${key}`)?.value || "";
            if (value && !(key === "filter-budget" && value === "3500")) count++;
        });
        $("#active-filter-count").textContent = String(count);
    }

    let filterTimeout;
    function reloadCurrentTab(showSkeleton = false) {
        window.clearTimeout(filterTimeout);
        countFilters();
        const requestedTab = state.tab;
        filterTimeout = window.setTimeout(async () => {
            if (showSkeleton) setLoading(true);
            const endpoint = requestedTab === "projetos" ? "/hall/api/projetos" : "/hall/api/influenciadores";
            try {
                const results = await request(`${endpoint}?${activeFilterParams()}`, { method: "GET", headers: {} });
                if (requestedTab === "projetos") {
                    state.projects = results;
                    if (state.tab === requestedTab) renderProjects();
                } else if (state.tab === "influenciadores") {
                    state.influencers = results;
                    if (state.tab === requestedTab) renderInfluencers();
                }
                updateTabCounts();
            } catch (error) {
                showToast(error.message, "error");
            } finally {
                if (showSkeleton) setLoading(false);
            }
        }, showSkeleton ? 220 : 140);
    }

    function activateTab(tab, animate = true) {
        state.tab = tab;
        $$(".hall-tab").forEach(button => {
            const active = button.dataset.tab === tab;
            button.classList.toggle("is-active", active);
            button.setAttribute("aria-pressed", String(active));
        });
        $$(".hall-panel").forEach(panel => {
            panel.hidden = panel.dataset.panel !== tab;
        });
        if (animate) setLoading(true);
        if (tab === "propostas") {
            request("/hall/api/propostas", { method: "GET", headers: {} })
                .then(proposals => {
                    state.proposals = proposals;
                    renderProposals();
                })
                .catch(error => showToast(error.message, "error"))
                .finally(() => setLoading(false));
        } else {
            reloadCurrentTab(animate);
        }
    }

    function projectById(id) {
        return state.projects.find(project => String(project.id) === String(id))
            || window.HALL_INITIAL_DATA.projects.find(project => String(project.id) === String(id));
    }

    function influencerById(id) {
        return state.influencers.find(influencer => String(influencer.id) === String(id))
            || window.HALL_INITIAL_DATA.influencers.find(influencer => String(influencer.id) === String(id));
    }

    function openProjectDetails(project) {
        if (!project) return;
        $("#details-eyebrow").textContent = `${project.category} · ${project.network}`;
        $("#details-title").textContent = project.title;
        $("#details-body").innerHTML = `<div class="detail-grid"><p><strong>Empresa</strong><span>${escapeHtml(project.company)}</span></p><p><strong>Orçamento</strong><span>${brl.format(project.budgetMin || project.budget)} – ${brl.format(project.budgetMax || project.budget)}</span></p><p><strong>Prazo</strong><span>${escapeHtml(project.deadline)} dias</span></p><p><strong>Bairro</strong><span>${escapeHtml(project.neighborhood)}</span></p><p><strong>Faixa de seguidores</strong><span>${number.format(project.followersMin || 0)} a ${number.format(project.followersMax || 50000)}</span></p><p><strong>Propostas</strong><span>${number.format(project.proposalCount || 0)}</span></p></div><p class="detail-description">${escapeHtml(project.description)}</p><p class="demo-notice"><i class="fa-solid fa-shield-halved" aria-hidden="true"></i> O pagamento protegido está em modo demonstrativo.</p>`;
        $("#details-actions").innerHTML = state.role === "INFLUENCER"
            ? `<button class="btn-primary-hall" data-action="send-proposal" data-id="${escapeHtml(project.id)}" data-close-details><i class="fa-solid fa-paper-plane" aria-hidden="true"></i> Enviar proposta</button>`
            : `<button class="btn-outline-hall" data-bs-dismiss="modal">Fechar</button>`;
        bootstrap.Modal.getOrCreateInstance($("#details-modal")).show();
    }

    function openInfluencerDetails(influencer) {
        if (!influencer) return;
        $("#details-eyebrow").textContent = `${influencer.niche} · ${influencer.network}`;
        $("#details-title").textContent = influencer.name;
        const avatar = safeUrl(influencer.avatar);
        const packages = (influencer.packages || []).map(pack => `<p class="package-line"><strong>${escapeHtml(pack.title)}</strong><span>A partir de ${brl.format(pack.price)}</span></p>`).join("");
        $("#details-body").innerHTML = `<div class="creator-detail">${avatar ? `<img src="${escapeHtml(avatar)}" alt="Foto de ${escapeHtml(influencer.name)}">` : ""}<div><strong>${escapeHtml(influencer.handle)}</strong><span>${escapeHtml(influencer.neighborhood)} · ${number.format(influencer.followers)} seguidores</span><span class="influencer-rating"><i class="fa-solid fa-star" aria-hidden="true"></i> ${Number(influencer.rating).toFixed(1)} avaliação</span></div></div><p class="detail-description">${escapeHtml(influencer.bio)}</p><h3 class="detail-subtitle">Pacotes disponíveis</h3>${packages || `<p>A partir de ${brl.format(influencer.price)}</p>`}`;
        $("#details-actions").innerHTML = state.role === "EMPRESA"
            ? `<button class="btn-primary-hall" data-action="invite-influencer" data-id="${escapeHtml(influencer.id)}" data-close-details><i class="fa-solid fa-paper-plane" aria-hidden="true"></i> Convidar</button>`
            : `<button class="btn-outline-hall" data-bs-dismiss="modal">Fechar</button>`;
        bootstrap.Modal.getOrCreateInstance($("#details-modal")).show();
    }

    function openProposalForm(project) {
        if (!project) return;
        bootstrap.Modal.getInstance($("#details-modal"))?.hide();
        $("#proposal-project-name").textContent = project.title;
        $("#proposal-project-id").value = project.id;
        $("#proposal-amount").value = project.budget || project.budgetMin || "";
        $("#proposal-deadline").value = project.deadline || 7;
        $("#proposal-message").value = "";
        $("#proposal-portfolio").value = "";
        updateProposalSummary();
        bootstrap.Modal.getOrCreateInstance($("#proposal-modal")).show();
    }

    function updateProposalSummary() {
        const amount = Math.max(0, Number($("#proposal-amount").value) || 0);
        const fee = amount * TAXA_PLATAFORMA;
        $("#fee-percent").textContent = `(${(TAXA_PLATAFORMA * 100).toLocaleString("pt-BR")}%)`;
        $("#proposal-fee").textContent = brl.format(fee);
        $("#proposal-net").textContent = brl.format(amount - fee);
    }

    async function toggleFavorite(button) {
        const { type, id } = button.dataset;
        try {
            const result = await request(`/hall/api/favoritos/${encodeURIComponent(id)}`, {
                method: "POST",
                body: JSON.stringify({ tipo: type })
            });
            setFavorite(type, id, result.favorito);
            button.classList.toggle("is-favorite", result.favorito);
            button.setAttribute("aria-pressed", String(result.favorito));
            button.setAttribute("aria-label", result.favorito ? "Remover dos favoritos" : "Adicionar aos favoritos");
            button.innerHTML = `<i class="fa-${result.favorito ? "solid" : "regular"} fa-heart" aria-hidden="true"></i>`;
            showToast(result.favorito ? "Adicionado aos favoritos." : "Removido dos favoritos.");
        } catch (error) {
            showToast(error.message, "error");
        }
    }

    function publishStep(step) {
        $$(".publish-step").forEach(section => {
            section.hidden = Number(section.dataset.step) !== step;
        });
        $("#publish-progress-bar").style.width = `${(step / 3) * 100}%`;
        $(".publish-progress").setAttribute("aria-valuenow", String(step));
        $("#publish-step-label").textContent = `Etapa ${step} de 3`;
        $("#publish-previous").hidden = step === 1;
        $("#publish-next").hidden = step === 3;
        $("#publish-submit").hidden = step !== 3;
        $("#publish-form").dataset.step = String(step);
    }

    function validCurrentStep() {
        const current = $(`.publish-step[data-step="${$("#publish-form").dataset.step}"]`);
        const fields = $$("input,select,textarea", current);
        for (const field of fields) {
            if (!field.reportValidity()) return false;
        }
        return true;
    }

    function updatePreview() {
        $("#preview-title").textContent = $("#publish-title-input").value || "Título do projeto";
        $("#preview-category").textContent = $("#publish-category").value || "Categoria";
        $("#preview-company").innerHTML = `<i class="fa-solid fa-store" aria-hidden="true"></i>${escapeHtml(window.HALL_INITIAL_DATA.profile.nome || "Sua empresa")}`;
        $("#preview-description").textContent = $("#publish-description").value || "A descrição da sua campanha aparecerá aqui.";
        const budget = Number($("#publish-budget").value);
        $("#preview-budget").textContent = budget ? brl.format(budget) : "Defina o orçamento";
        $("#preview-deadline").textContent = $("#publish-deadline").value ? `${$("#publish-deadline").value} dias` : "Prazo";
    }

    async function publishProject(event) {
        event.preventDefault();
        if (!validCurrentStep()) return;
        const formData = new FormData(event.currentTarget);
        const payload = Object.fromEntries(formData.entries());
        ["orcamento", "seguidoresMin", "seguidoresMax", "prazoDias"].forEach(key => {
            payload[key] = Number(payload[key]);
        });
        try {
            const result = await request("/hall/api/company/projetos", {
                method: "POST",
                body: JSON.stringify(payload)
            });
            state.projects.unshift(result.project);
            state.ownProjectIds.add(String(result.project.id));
            updateTabCounts();
            renderProjects();
            bootstrap.Modal.getInstance($("#publish-modal")).hide();
            event.currentTarget.reset();
            publishStep(1);
            updatePreview();
            showToast(result.message);
            activateTab("projetos", false);
        } catch (error) {
            showToast(error.message, "error");
        }
    }

    async function submitProposal(event) {
        event.preventDefault();
        const amount = Number($("#proposal-amount").value);
        if (amount <= 0) {
            $("#proposal-amount").focus();
            return;
        }
        const payload = {
            projectId: Number($("#proposal-project-id").value),
            valor: amount,
            prazoDias: Number($("#proposal-deadline").value),
            mensagem: $("#proposal-message").value.trim(),
            linkPortfolio: $("#proposal-portfolio").value.trim() || null
        };
        try {
            const result = await request("/hall/api/influencer/propostas", {
                method: "POST",
                body: JSON.stringify(payload)
            });
            const project = projectById(payload.projectId);
            if (project) {
                project.proposalCount = Number(project.proposalCount || 0) + 1;
                if (state.tab === "projetos") renderProjects();
            }
            bootstrap.Modal.getInstance($("#proposal-modal")).hide();
            showToast(result.message);
        } catch (error) {
            showToast(error.message, "error");
        }
    }

    async function inviteInfluencer(id) {
        if (!state.ownProjectIds.size) {
            showToast("Publique um projeto nesta sessão para convidar um influenciador.", "error");
            return;
        }
        const projectId = [...state.ownProjectIds][0];
        try {
            const result = await request(`/hall/api/company/projetos/${encodeURIComponent(projectId)}/convites/${encodeURIComponent(id)}`, {
                method: "POST",
                body: "{}"
            });
            showToast(result.message);
        } catch (error) {
            showToast(error.message, "error");
        }
    }

    async function updateProposalStatus(id, status) {
        try {
            const result = await request(`/hall/api/company/propostas/${encodeURIComponent(id)}/status`, {
                method: "POST",
                body: JSON.stringify({ status })
            });
            state.proposals = state.proposals.map(proposal => String(proposal.id) === String(id) ? result.proposal : proposal);
            renderProposals();
            showToast(result.message);
            if (status === "ACEITA") confetti();
        } catch (error) {
            showToast(error.message, "error");
        }
    }

    function confetti() {
        const colors = ["#ff6b6b", "#f3c76a", "#4dd2a5", "#e7a3ff", "#fff"];
        for (let index = 0; index < 38; index++) {
            const piece = document.createElement("i");
            piece.className = "confetti-piece";
            piece.style.left = `${Math.random() * 100}vw`;
            piece.style.background = colors[index % colors.length];
            piece.style.animationDelay = `${Math.random() * 350}ms`;
            document.body.append(piece);
            window.setTimeout(() => piece.remove(), 1900);
        }
    }

    function showComparison() {
        const items = state.proposals.filter(proposal => state.compared.has(proposal.id)).slice(0, 3);
        $("#comparison-grid").innerHTML = items.map(proposal => `<article class="comparison-item"><h3>${escapeHtml(proposal.influencer)}</h3><p>${escapeHtml(proposal.projectTitle)}</p><strong>${brl.format(proposal.amount)}</strong><p>Prazo: ${escapeHtml(proposal.deadline)} dias</p><p>${escapeHtml(proposal.message)}</p><span class="status-badge">${escapeHtml(proposal.status)}</span></article>`).join("");
        bootstrap.Modal.getOrCreateInstance($("#compare-modal")).show();
    }

    function updateCompareControls() {
        $("#compare-count").textContent = String(state.compared.size);
        $("#compare-button").disabled = state.compared.size < 2;
        $$('input[data-action="compare"]').forEach(input => {
            input.disabled = !input.checked && state.compared.size >= 3;
        });
    }

    function scheduleActivity() {
        window.clearTimeout(state.activityTimer);
        state.activityTimer = window.setTimeout(() => {
            const activityTypes = state.role === "EMPRESA"
                ? ["projeto", "influenciador", "proposta"]
                : ["projeto", "influenciador"];
            const type = activityTypes[Math.floor(Math.random() * activityTypes.length)];
            const descriptions = {
                projeto: ["Nova oportunidade no Hall", "Uma empresa local publicou uma campanha demonstrativa."],
                influenciador: ["Novo criador ativo na região", "Um perfil local apareceu na vitrine demonstrativa."],
                proposta: ["Movimento nas propostas", "Uma proposta demonstrativa movimentou o Hall."]
            };
            state.liveActivity = { type, title: descriptions[type][0], detail: descriptions[type][1] };
            if (type === "projeto" && state.tab === "projetos") renderProjects();
            if (type === "influenciador" && state.tab === "influenciadores") renderInfluencers();
            const badge = $("#notification-count");
            badge.textContent = String((Number(badge.textContent) || 0) + 1);
            badge.style.animation = "none";
            requestAnimationFrame(() => { badge.style.animation = "notification-pop .35s ease, notification-pulse 2s infinite .35s"; });
            showToast(`Atividade simulada: ${descriptions[type][0].toLowerCase()}.`);
            window.setTimeout(() => {
                state.liveActivity = null;
                if (state.tab === "projetos") renderProjects();
                if (state.tab === "influenciadores") renderInfluencers();
            }, 12000);
            scheduleActivity();
        }, 8000 + Math.random() * 7000);
    }

    function initializeMobileFilters() {
        const copy = $("#desktop-filters").cloneNode(true);
        copy.id = "mobile-filters";
        copy.querySelectorAll("[id]").forEach(element => {
            const originalId = element.id;
            element.id = `${originalId}-mobile`;
            if (element.tagName === "LABEL") {
                element.querySelectorAll("[id]").forEach(child => {
                    const match = child.id.match(/^(.*)-mobile$/);
                    if (match) child.id = `${match[1]}-mobile`;
                });
            }
        });
        $("#mobile-filter-controls").append(copy);
    }

    function syncFilter(source) {
        const baseId = source.id.replace(/-mobile$/, "");
        const other = $(`#${baseId}-mobile`) === source ? $(`#${baseId}`) : $(`#${baseId}-mobile`);
        if (other) other.value = source.value;
        if (baseId === "filter-budget") {
            $$("#budget-label").forEach(label => label.textContent = `até ${brl.format(Number(source.value))}`);
            $$("#budget-label-mobile").forEach(label => label.textContent = `até ${brl.format(Number(source.value))}`);
        }
    }

    function resetFilters() {
        const defaults = {
            "filter-category": "", "filter-network": "", "filter-budget": "3500",
            "filter-followers": "", "filter-neighborhood": "", "filter-deadline": "", "filter-sort": "recentes"
        };
        Object.entries(defaults).forEach(([id, value]) => {
            const main = $(`#${id}`);
            const mobile = $(`#${id}-mobile`);
            if (main) main.value = value;
            if (mobile) mobile.value = value;
        });
        $("#budget-label").textContent = "até R$ 3.500";
        $("#budget-label-mobile").textContent = "até R$ 3.500";
        $("#hall-search").value = "";
        reloadCurrentTab();
    }

    document.addEventListener("click", event => {
        const tab = event.target.closest("[data-tab]");
        if (tab) {
            activateTab(tab.dataset.tab);
            return;
        }
        const control = event.target.closest("[data-action]");
        if (!control) return;
        const { action, id } = control.dataset;
        if (action === "favorite") toggleFavorite(control);
        if (action === "project-details") openProjectDetails(projectById(id));
        if (action === "influencer-details") openInfluencerDetails(influencerById(id));
        if (action === "send-proposal") openProposalForm(projectById(id));
        if (action === "invite-influencer") inviteInfluencer(id);
        if (action === "accept-proposal") updateProposalStatus(Number(id), "ACEITA");
        if (action === "reject-proposal") updateProposalStatus(Number(id), "RECUSADA");
        if (action === "message-proposal") showToast("A conversa estará disponível quando o chat for integrado.");
    });

    document.addEventListener("change", event => {
        const filter = event.target.closest("#desktop-filters select, #desktop-filters input, #mobile-filters select, #mobile-filters input");
        if (filter) {
            syncFilter(filter);
            reloadCurrentTab();
        }
        if (event.target.matches('input[data-action="compare"]')) {
            const id = Number(event.target.value);
            if (event.target.checked && state.compared.size < 3) state.compared.add(id);
            else state.compared.delete(id);
            updateCompareControls();
        }
    });

    document.addEventListener("input", event => {
        const filter = event.target.closest("#desktop-filters select, #desktop-filters input, #mobile-filters select, #mobile-filters input");
        if (filter) {
            syncFilter(filter);
            if (filter.id.startsWith("filter-budget")) {
                const value = `até ${brl.format(Number(filter.value))}`;
                $("#budget-label").textContent = value;
                $("#budget-label-mobile").textContent = value;
            }
            reloadCurrentTab();
        }
        if (event.target.id === "hall-search") reloadCurrentTab();
        if (event.target.matches("#proposal-amount")) updateProposalSummary();
        if (event.target.closest("#publish-form")) updatePreview();
    });

    $("#clear-filters").addEventListener("click", resetFilters);
    document.addEventListener("click", event => {
        if (event.target.closest("#clear-filters-mobile")) resetFilters();
    });
    $("#proposal-form").addEventListener("submit", submitProposal);
    $("#publish-form").addEventListener("submit", publishProject);
    $("#publish-next").addEventListener("click", () => {
        if (!validCurrentStep()) return;
        const step = Number($("#publish-form").dataset.step);
        if (step < 3) publishStep(step + 1);
        updatePreview();
    });
    $("#publish-previous").addEventListener("click", () => publishStep(Math.max(1, Number($("#publish-form").dataset.step) - 1)));
    $("#publish-title-input").addEventListener("input", updatePreview);
    $("#publish-category").addEventListener("change", updatePreview);
    $("#publish-description").addEventListener("input", updatePreview);
    $("#publish-budget").addEventListener("input", updatePreview);
    $("#publish-deadline").addEventListener("input", updatePreview);
    $("#compare-button").addEventListener("click", showComparison);

    document.addEventListener("keydown", event => {
        if (event.key === "/" && !["INPUT", "TEXTAREA", "SELECT"].includes(document.activeElement.tagName)) {
            event.preventDefault();
            $("#hall-search").focus();
        }
    });

    $("#details-modal").addEventListener("hidden.bs.modal", () => {
        if (document.activeElement?.dataset?.closeDetails !== undefined) document.activeElement.blur();
    });

    initializeMobileFilters();
    publishStep(1);
    updatePreview();
    animateCounters();
    renderProjects();
    renderInfluencers();
    renderProposals();
    updateTabCounts();
    activateTab(state.tab, false);
    setLoading(true);
    window.setTimeout(() => setLoading(false), 620);
    scheduleActivity();
})();
