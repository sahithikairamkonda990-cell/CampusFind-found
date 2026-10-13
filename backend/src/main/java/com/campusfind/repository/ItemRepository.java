package com.campusfind.repository;

import com.campusfind.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("select i from Item i where " +
           "(:q is null or lower(i.itemName) like lower(concat('%', :q, '%')) or lower(i.description) like lower(concat('%', :q, '%'))) and " +
           "(:type is null or i.type = :type) and " +
           "(:category is null or i.category = :category)")
    List<Item> search(@Param("q") String q, @Param("type") String type, @Param("category") String category);
}
