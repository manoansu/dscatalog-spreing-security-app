package pt.amane.dscatalog.services;

import static org.mockito.Mockito.doThrow;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import pt.amane.dscatalog.dtos.ProductDTO;
import pt.amane.dscatalog.entities.Category;
import pt.amane.dscatalog.entities.Product;
import pt.amane.dscatalog.repositories.CategoryRepository;
import pt.amane.dscatalog.repositories.ProductRepository;
import pt.amane.dscatalog.services.exceptions.DataBaseIntegrityViolationException;
import pt.amane.dscatalog.services.exceptions.ResourceNotFoundException;
import pt.amane.dscatalog.tests.Factory;

@ExtendWith(SpringExtension.class)
class ProductServiceTests {

	// permite fazer o teste só para a entidade service se
	// incluir componente de integração
	@InjectMocks
	private ProductService service;

	// permite fazer o teste para nao incluir o contexto da aplicação..
	@Mock
	private ProductRepository repository;
	
	@Mock
	private CategoryRepository categoryRepository;

	private long existingId;
	private long nonExistingId;
	private long dependentId;
	private PageImpl<Product> page;
	private Product product;
	private Category category;
	ProductDTO productDTO;

	@BeforeEach
	void setUp() throws Exception {
		existingId = 1L;
		nonExistingId = 10000L;
		dependentId = 3L;
		product = Factory.createProduct();
		category = Factory.createCategory();
		productDTO = Factory.createProductDTO();
		page = new PageImpl<>(List.of(product));

		// usando o Mock, ainda tem que configurar o
		// comportamento do metodo desejado, nesse caso é deleById(id);
		// Mockito.when(null) espera o retorno de alguma coisa na função..
		
		//Quando eu passar um metodo findAll 
		//passando qualquer argumento do tipo Pegeable, esse objecto retorna o tipo page..
		Mockito.when(repository.findAll((Pageable) ArgumentMatchers.any())).thenReturn(page);

		Mockito.when(repository.save(ArgumentMatchers.any())).thenReturn(product);
		
		// of() retorna um objecto no metodo optional..
		Mockito.when(repository.findById(existingId)).thenReturn(Optional.of(product));
		
		Mockito.when(repository.findById(nonExistingId)).thenReturn(Optional.of(product));
		
		Mockito.when(repository.getOne(existingId)).thenReturn(product);
		
		Mockito.when(repository.getOne(nonExistingId)).thenThrow(EntityNotFoundException.class);
		
		Mockito.when(categoryRepository.getOne(existingId)).thenReturn(category);
		
		Mockito.when(categoryRepository.getOne(nonExistingId)).thenThrow(EntityNotFoundException.class);
		
		
		
		// nao faça ou nao retorna nada nesse metodo..
		Mockito.doNothing().when(repository).deleteById(existingId);

		// pega exception quando passamos id q nao existe no delete Bd..
		Mockito.doThrow(EmptyResultDataAccessException.class).when(repository).deleteById(nonExistingId);

		// Mockito pode ser feito um import satico. por ex:
		// doNothing().when(repository).deleteById(existingId);
		// e outros sem passar Mockito.nome de metodo..
		doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);

	}
	
	@Test
	void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.update(nonExistingId, productDTO);
		});
	}
	
	@Test
	void updateShouldReturnProductDTOWhenIdExists() {
		ProductDTO result = service.update(existingId, productDTO);
		Assertions.assertNotNull(result);
	}
	
	@Test
	void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.findById(nonExistingId);
		});
	}
	
	@Test
	void findByIdShouldReturnProductDTOWhenExists() {
		ProductDTO result = service.findById(existingId);
		Assertions.assertNotNull(result);
	}
	
		
	@Test
	void findAllPagedShouldReturnPage() {
		Pageable pageable = PageRequest.of(0, 12);
		Page<ProductDTO> result = service.findAllPaged(pageable);
		Assertions.assertNotNull(result);
		Mockito.verify(repository).findAll(pageable);
	}

	@Test
	void deleteShouldDoNothingWhenIdExists() {
		Assertions.assertDoesNotThrow(() -> {
			service.delete(existingId);
		});

		// verifica se o metodo deleteById foi chamado pelo repository..
		Mockito.verify(repository, Mockito.times(1)).deleteById(existingId);

	}

	@Test
	void deleteShouldThrowEmptyResourceNotFoundExceptionWhenIdDoesNotExists() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(nonExistingId);
		});

		// verifica se o metodo deleteById foi chamado pelo repository..
		Mockito.verify(repository, Mockito.times(1)).deleteById(nonExistingId);

	}

	@Test
	void deleteShouldThrowDataIntegrityViolationExceptionWhenIdDoesNotExists() {
		Assertions.assertThrows(DataBaseIntegrityViolationException.class, () -> {
			service.delete(dependentId);
		});

		// verifica se o metodo deleteById foi chamado pelo repository..
		Mockito.verify(repository, Mockito.times(1)).deleteById(dependentId);

	}
}
