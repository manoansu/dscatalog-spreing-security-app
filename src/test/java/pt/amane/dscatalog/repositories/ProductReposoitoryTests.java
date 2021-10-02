package pt.amane.dscatalog.repositories;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;

import pt.amane.dscatalog.entities.Product;
import pt.amane.dscatalog.tests.Factory;

@DataJpaTest
class ProductReposoitoryTests {
	
	@Autowired
	private ProductRepository repository;
	
	private long existingId;
	private long nonExistingId;
	private long countTotalProducts;
	

	@BeforeEach
	void setUp() throws Exception {
		existingId = 1L;
		nonExistingId = 1000L;
		countTotalProducts = 25L;
	}
	
	@Test
	void saveShouldPersisteWithAutoIncrementWhenIdIsNull() {
		
		Product product = Factory.createProduct();
		product.setId(null);
		
		product = repository.save(product);
		Optional<Product> result = repository.findById(product.getId());
		
		//result.isPresent() ==> é o metodo de Optional que verifica se objecto existe.
		
		Assertions.assertNotNull(product.getId());
		Assertions.assertEquals(countTotalProducts + 1, product.getId());
		Assertions.assertTrue(result.isPresent());
		Assertions.assertSame(result.get(), product);
				
	}
	
	@Test
	void findByIdShouldReturnNonEmptyOptionalWhenIdExists() {
		
		Optional<Product> result = repository.findById(existingId);
		
		Assertions.assertTrue(result.isPresent());
	}
	
	@Test
	void findByIdShouldReturnNonEmptyOptionalWhenIdDoesNotExists() {
		
		Optional<Product> result = repository.findById(nonExistingId);
		
		//result.isEmpty() ==> é o metodo de Optional que verifica se objecto esta vazio.
		
		Assertions.assertTrue(result.isEmpty());
	}
	
	
	@Test
	void deleteShouldDeleteObjectWhenIdExists() {
		
		repository.deleteById(existingId);
		
		Optional<Product> result = repository.findById(existingId);
		
		Assertions.assertFalse(result.isPresent()); // verifica se o id existe no banco.
		
	}
	
	@Test
	void deleteShouldRhrowEmptyResultDataAccessExceptionWhenIdDoesNotExist() {
		
		Assertions.assertThrows(EmptyResultDataAccessException.class, () -> {
			repository.deleteById(nonExistingId);
		});
	}

}
