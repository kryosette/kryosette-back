//package com.example.demo.user;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.data.domain.Example;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.repository.query.FluentQuery;
//import org.springframework.stereotype.Service;
//import org.webjars.NotFoundException;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.function.Function;
//
//@Service
//public class UserServiceImpl implements UserRepository {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Override
//    public List<String> findAllUsernames() {
//        return List.of();
//    }
//
//    @Override
//    public Optional<User> findByEmail(String email) {
//        return Optional.empty();
//    }
//
//    @Override
//    public User updateUser(String userId, String newName) throws NotFoundException {
//        // 1. Найти пользователя в базе данных по userId
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
//
//        // 2. Обновить имя пользователя
//        user.setFirstname(newName);
//
//        // 3. Сохранить обновленного пользователя в базе данных
//        return userRepository.save(user);
//    }
//
//    @Override
//    public void flush() {
//
//    }
//
//    @Override
//    public <S extends User> S saveAndFlush(S entity) {
//        return null;
//    }
//
//    @Override
//    public <S extends User> List<S> saveAllAndFlush(Iterable<S> entities) {
//        return List.of();
//    }
//
//    @Override
//    public void deleteAllInBatch(Iterable<User> entities) {
//
//    }
//
//    @Override
//    public void deleteAllByIdInBatch(Iterable<String> strings) {
//
//    }
//
//    @Override
//    public void deleteAllInBatch() {
//
//    }
//
//    @Override
//    public User getOne(String s) {
//        return null;
//    }
//
//    @Override
//    public User getById(String s) {
//        return null;
//    }
//
//    @Override
//    public User getReferenceById(String s) {
//        return null;
//    }
//
//    @Override
//    public <S extends User> Optional<S> findOne(Example<S> example) {
//        return Optional.empty();
//    }
//
//    @Override
//    public <S extends User> List<S> findAll(Example<S> example) {
//        return List.of();
//    }
//
//    @Override
//    public <S extends User> List<S> findAll(Example<S> example, Sort sort) {
//        return List.of();
//    }
//
//    @Override
//    public <S extends User> Page<S> findAll(Example<S> example, Pageable pageable) {
//        return null;
//    }
//
//    @Override
//    public <S extends User> long count(Example<S> example) {
//        return 0;
//    }
//
//    @Override
//    public <S extends User> boolean exists(Example<S> example) {
//        return false;
//    }
//
//    @Override
//    public <S extends User, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
//        return null;
//    }
//
//    @Override
//    public <S extends User> S save(S entity) {
//        return null;
//    }
//
//    @Override
//    public <S extends User> List<S> saveAll(Iterable<S> entities) {
//        return List.of();
//    }
//
//    @Override
//    public Optional<User> findById(String s) {
//        return Optional.empty();
//    }
//
//    @Override
//    public boolean existsById(String s) {
//        return false;
//    }
//
//    @Override
//    public List<User> findAll() {
//        return List.of();
//    }
//
//    @Override
//    public List<User> findAllById(Iterable<String> strings) {
//        return List.of();
//    }
//
//    @Override
//    public long count() {
//        return 0;
//    }
//
//    @Override
//    public void deleteById(String s) {
//
//    }
//
//    @Override
//    public void delete(User entity) {
//
//    }
//
//    @Override
//    public void deleteAllById(Iterable<? extends String> strings) {
//
//    }
//
//    @Override
//    public void deleteAll(Iterable<? extends User> entities) {
//
//    }
//
//    @Override
//    public void deleteAll() {
//
//    }
//
//    @Override
//    public List<User> findAll(Sort sort) {
//        return List.of();
//    }
//
//    @Override
//    public Page<User> findAll(Pageable pageable) {
//        return null;
//    }
//}