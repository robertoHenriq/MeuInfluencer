package api.repositorys;

import api.models.Influencer;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Repository
public class InfluencerRepository implements JpaRepository<Influencer, Integer> {

    @Override
    public void flush() {

    }

    @Override
    public <S extends Influencer> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends Influencer> List<S> saveAllAndFlush(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public void deleteAllInBatch(Iterable<Influencer> entities) {

    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Integer> integers) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public Influencer getOne(Integer integer) {
        return null;
    }

    @Override
    public Influencer getById(Integer integer) {
        return null;
    }

    @Override
    public Influencer getReferenceById(Integer integer) {
        return null;
    }

    @Override
    public <S extends Influencer> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends Influencer> List<S> findAll(Example<S> example) {
        return List.of();
    }

    @Override
    public <S extends Influencer> List<S> findAll(Example<S> example, Sort sort) {
        return List.of();
    }

    @Override
    public <S extends Influencer> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Influencer> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Influencer> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends Influencer, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public <S extends Influencer> S save(S entity) {
        return null;
    }

    @Override
    public <S extends Influencer> List<S> saveAll(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public Optional<Influencer> findById(Integer integer) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Integer integer) {
        return false;
    }

    @Override
    public List<Influencer> findAll() {
        return List.of();
    }

    @Override
    public List<Influencer> findAllById(Iterable<Integer> integers) {
        return List.of();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Integer integer) {

    }

    @Override
    public void delete(Influencer entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends Integer> integers) {

    }

    @Override
    public void deleteAll(Iterable<? extends Influencer> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public List<Influencer> findAll(Sort sort) {
        return List.of();
    }

    @Override
    public Page<Influencer> findAll(Pageable pageable) {
        return null;
    }
}
