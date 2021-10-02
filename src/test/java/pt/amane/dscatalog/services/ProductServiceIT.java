package pt.amane.dscatalog.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import pt.amane.dscatalog.dtos.ProductDTO;
import pt.amane.dscatalog.repositories.ProductRepository;
import pt.amane.dscatalog.services.exceptions.ResourceNotFoundException;

/**
 * Transational => faz com que cada teste roda independente ou seja cada teste
 * que roda ele faz o rolback para o novo teste rodar sem depender ou
 * influenciar no teste de outro.
 * 
 * @author manoa
 *
 */
@SpringBootTest
@Transactional
class ProductServiceIT {

	// esse atributo é usado para interter com base de
	// dado usando teste de integração.
	@Autowired
	private ProductService service;

	@Autowired
	private ProductRepository repository;

	private Long existingId;
	private Long nonExisting;
	private Long countTotalProducts;

	@BeforeEach
	void setUp() throws Exception {

		existingId = 1L;
		nonExisting = 1000L;
		countTotalProducts = 25L;
	}

	@Test
	void deleteShouldDeleteResourceWhenIdExists() {

		service.delete(existingId);

		Assertions.assertEquals(countTotalProducts - 1, repository.count());
	}

	@Test
	void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(nonExisting);
		});
	}

	@Test
	void findAllPagedShouldReturnPageWhenPage0Size10() {

		PageRequest pageRequest = PageRequest.of(0, 10);

		Page<ProductDTO> result = service.findAllPaged(pageRequest);

		Assertions.assertFalse(result.isEmpty());
		Assertions.assertEquals(0, result.getNumber());
		Assertions.assertEquals(10, result.getSize());
		Assertions.assertEquals(countTotalProducts, result.getTotalElements());
	}

	@Test
	void findAllPagedShouldReturnEmpyPageWhenPageDoesNotExist() {

		PageRequest pageRequest = PageRequest.of(50, 10);

		Page<ProductDTO> result = service.findAllPaged(pageRequest);

		// assertTrue esta afirmar que a pagina esta vazia..
		Assertions.assertTrue(result.isEmpty());
	}
	
	@Test
	void findAllPagedShouldReturnSortedPageWhenSortByName() {

		PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name"));

		Page<ProductDTO> result = service.findAllPaged(pageRequest);

		// assertFalse esta afirmar que a pagina não esta vazia..
		Assertions.assertFalse(result.isEmpty());
		
		Assertions.assertEquals("Macbook Pro", result.getContent().get(0).getName());
		Assertions.assertEquals("PC Gamer", result.getContent().get(1).getName());
		Assertions.assertEquals("PC Gamer Alfa", result.getContent().get(2).getName());
	}

}
