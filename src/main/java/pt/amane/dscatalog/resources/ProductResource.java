package pt.amane.dscatalog.resources;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import pt.amane.dscatalog.dtos.ProductDTO;
import pt.amane.dscatalog.services.ProductService;

@RestController
@RequestMapping(value = "/products")
public class ProductResource {

	
	@Autowired
	private ProductService service;

	@GetMapping(value = "/{id}")
	public ResponseEntity<ProductDTO> findById(@PathVariable Long id) {
		ProductDTO dto = service.findById(id);
		return ResponseEntity.ok().body(dto);
	}

	@GetMapping
	public ResponseEntity<Page<ProductDTO>> finadAll(Pageable pageable){
		
		// PARAMETROS: page, size, sort
		Page<ProductDTO> list = service.findAllPaged(pageable);
		return ResponseEntity.ok().body(list);
	}

	@PostMapping
	public ResponseEntity<ProductDTO> create(@RequestBody ProductDTO dto) {
		dto = service.create(dto);
		URI url = ServletUriComponentsBuilder.fromCurrentContextPath().path("/{id}").buildAndExpand(dto.getId())
				.toUri();
		return ResponseEntity.created(url).build();
	}

	@PutMapping(value = "/{id}")
	public ResponseEntity<ProductDTO> update(@PathVariable Long id, @RequestBody ProductDTO dto) {
		dto = service.update(id, dto);
		return ResponseEntity.ok().body(dto);
	}

	@DeleteMapping(value = "/{id}")
	public ResponseEntity<ProductDTO> delete(@PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}

}
