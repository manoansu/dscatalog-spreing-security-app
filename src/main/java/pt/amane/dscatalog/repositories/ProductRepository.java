package pt.amane.dscatalog.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import pt.amane.dscatalog.entities.Category;
import pt.amane.dscatalog.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	@Query("SELECT DISTINCT obj FROM Product obj INNER JOIN obj.categories cats "
			+ "WHERE (:category IS NULL OR :category IN cats)")
	Page<Product> findAllIdByCategory(Category category, Pageable pageable);

}
