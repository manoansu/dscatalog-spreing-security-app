package pt.amane.dscatalog.services;

import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.amane.dscatalog.dtos.CategoryDTO;
import pt.amane.dscatalog.entities.Category;
import pt.amane.dscatalog.repositories.CategoryRepository;
import pt.amane.dscatalog.services.exceptions.DataBaseIntegrityViolationException;
import pt.amane.dscatalog.services.exceptions.ResourceNotFoundException;

@Service
public class CategoryService {

	@Autowired
	private CategoryRepository repository;

	@Transactional(readOnly = true)
	public CategoryDTO findById(Long id) {
		Optional<Category> obj = repository.findById(id);
		Category cat = obj.orElseThrow(() -> new ResourceNotFoundException(
				"Object not found! Id: " + id + ", Type: " + Category.class.getName()));
		return new CategoryDTO(cat);
	}

	@Transactional(readOnly = true)
	public Page<CategoryDTO> findAllPaged(Pageable pageable) {
		Page<Category> list = repository.findAll(pageable);
		return list.map(dto -> new CategoryDTO(dto));
	}

	@Transactional
	public CategoryDTO create(CategoryDTO categoryDTO) {
		Category category = new Category();
		category.setName(categoryDTO.getName());
		repository.save(category);
		return new CategoryDTO(category);
	}

	@Transactional
	public CategoryDTO update(Long id, CategoryDTO dto) {
		try {
			Category cat = repository.getOne(id);
			cat.setName(dto.getName());
			cat = repository.save(cat);
			return new CategoryDTO(cat);
		} catch (EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id not found! Id: " + id + ", Type: " + CategoryDTO.class.getName());
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

}
