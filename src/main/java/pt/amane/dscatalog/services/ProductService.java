package pt.amane.dscatalog.services;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.amane.dscatalog.dtos.ProductDTO;
import pt.amane.dscatalog.entities.Category;
import pt.amane.dscatalog.entities.Product;
import pt.amane.dscatalog.repositories.CategoryRepository;
import pt.amane.dscatalog.repositories.ProductRepository;
import pt.amane.dscatalog.services.exceptions.DataBaseIntegrityViolationException;
import pt.amane.dscatalog.services.exceptions.ResourceNotFoundException;

@Service
public class ProductService {

	@Autowired
	private ProductRepository repository;
	
	@Autowired
	private CategoryRepository categoryRepository;

	@Transactional(readOnly = true)
	public ProductDTO findById(Long id) {
		Optional<Product> productId = repository.findById(id);
		Product product = productId.orElseThrow(() -> new ResourceNotFoundException(
				"Object not found! Id: " + id + ", Type: " + ProductDTO.class.getName()));
		return new ProductDTO(product, product.getCategories());
	}

	@Transactional(readOnly = true)
	public Page<ProductDTO> findAllPaged(Long categoryId, String name, Pageable pageable) {
		List<Category> categories = (categoryId == 0) ? null : Arrays.asList(categoryRepository.getOne(categoryId));
		Page<Product> page = repository.findAllIdByCategory(categories,name,pageable);
		repository.findProductsWithCategories(page.getContent());
		return page.map(dto -> new ProductDTO(dto, dto.getCategories()));
	}

	@Transactional
	public ProductDTO create(ProductDTO dto) {
		Product product = new Product();
		copyDtoToProducty(dto, product);
		product = repository.save(product);
		return new ProductDTO(product);
	}

	@Transactional
	public ProductDTO update(Long id, ProductDTO dto) {
		try {
			Product product = repository.getOne(id);
			copyDtoToProducty(dto, product);
			product = repository.save(product);
			return new ProductDTO(product);
		} catch (EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id not found! Id: " + id + ", Type: " + ProductDTO.class.getName());
		}

	}

	
	public void delete(Long id) {
		try {
			repository.deleteById(id);
		} catch (EmptyResultDataAccessException e) {
			throw new ResourceNotFoundException("Id not found! Id: " + id);
		} catch (DataIntegrityViolationException e) {
			throw new DataBaseIntegrityViolationException("category cannot be deleted! has associated object..");
		}
	}

	private void copyDtoToProducty(ProductDTO dto, Product product) {

		product.setName(dto.getName());
		product.setDate(dto.getDate());
		product.setDescription(dto.getDescription());
		product.setImgUrl(dto.getImgUrl());
		product.setPrice(dto.getPrice());
		
		product.getCategories().clear();
		for(Category catDTO: product.getCategories()) {
			Category category = categoryRepository.getOne(catDTO.getId());
			product.getCategories().add(category);
		}
	}

}
