package com.onepath.repository;

import com.onepath.entity.ServiceCenter;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceCenterRepository extends JpaRepository<ServiceCenter, Long>, JpaSpecificationExecutor<ServiceCenter> {

    boolean existsBySlug(String slug);

    Optional<ServiceCenter> findBySlug(String slug);

    List<ServiceCenter> findByCityIgnoreCase(String city);

    List<ServiceCenter> findByCategoryIgnoreCase(String category);

    List<ServiceCenter> findByServiceTypeIgnoreCase(String serviceType);

    default List<ServiceCenter> findWithFilters(String category, String city, String serviceType, String search) {
        Specification<ServiceCenter> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (category != null && !category.trim().isEmpty() && !"ALL".equalsIgnoreCase(category.trim())) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase()));
            }

            if (city != null && !city.trim().isEmpty() && !"ALL".equalsIgnoreCase(city.trim())) {
                predicates.add(cb.equal(cb.lower(root.get("city")), city.trim().toLowerCase()));
            }

            if (serviceType != null && !serviceType.trim().isEmpty() && !"ALL".equalsIgnoreCase(serviceType.trim())) {
                predicates.add(cb.equal(cb.lower(root.get("serviceType")), serviceType.trim().toLowerCase()));
            }

            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("city")), pattern),
                        cb.like(cb.lower(root.get("district")), pattern),
                        cb.like(cb.lower(root.get("locality")), pattern),
                        cb.like(cb.lower(root.get("address")), pattern),
                        cb.like(cb.lower(root.get("supportedServices")), pattern)
                ));
            }

            if (query != null && Long.class != query.getResultType() && long.class != query.getResultType()) {
                Expression<Integer> vijayawadaFirst = cb.selectCase()
                        .when(cb.equal(cb.lower(root.get("city")), "vijayawada"), 0)
                        .otherwise(1)
                        .as(Integer.class);
                query.orderBy(cb.asc(vijayawadaFirst), cb.asc(root.get("city")), cb.asc(root.get("name")));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        return findAll(spec);
    }

    default List<ServiceCenter> searchServiceCenters(String query) {
        return findWithFilters(null, null, null, query);
    }
}
