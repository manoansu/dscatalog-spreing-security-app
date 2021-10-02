package pt.amane.dscatalog.resources;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import pt.amane.dscatalog.dtos.ProductDTO;
import pt.amane.dscatalog.tests.Factory;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductResourceIT {

	@Autowired
	private MockMvc mockMvc;

	// essa anotação é aceite pq o ObjectMapper ele auxilia o objeto
	// ele nao e a dependencia de produtoresource ou nao interfere em componente..
	@Autowired
	private ObjectMapper objectMapper;

	private ProductDTO productDTO;

	private Long existingId;
	private Long nonExistingId;
	private Long countTotalProducts;

	@BeforeEach
	void setUp() throws Exception {

		existingId = 1L;
		nonExistingId = 10000L;
		countTotalProducts = 25L;
		
		productDTO = Factory.createProductDTO();

	}

	@Test
	void findAllShouldReturnSortedPageWhenSortByName() throws Exception {

		ResultActions result = mockMvc
				.perform(get("/products?page=0&lsize=5&sort=name,asc").accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.totalElements").value(countTotalProducts));
		result.andExpect(jsonPath("$.content").exists());
		result.andExpect(jsonPath("$.content[0].name").value("Macbook Pro"));
		result.andExpect(jsonPath("$.content[1].name").value("PC Gamer"));
		result.andExpect(jsonPath("$.content[2].name").value("PC Gamer Alfa"));
	}

	@Test
	void updateShoudReturnProductDTOWhenIdExists() throws Exception {

		// converte o objecto em string..
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		String expectedName = productDTO.getName();
		String expectedDescription = productDTO.getDescription();

		ResultActions result = mockMvc.perform(put("/products/{id}", existingId)
				.content(jsonBody) // pega o conteudo de object em JSON
				.contentType(MediaType.APPLICATION_JSON) //O tipo de conteudo é JSON
				.accept(MediaType.APPLICATION_JSON)); // O tipo de dados é JSON.

		result.andExpect(status().isOk());
		// Esse metodo testa se existe esse atributo json.
		result.andExpect(jsonPath("$.id").value(existingId));
		result.andExpect(jsonPath("$.name").value(expectedName));
		result.andExpect(jsonPath("$.description").value(expectedDescription));
	}
	
	@Test
	void updateShoudReturnNotFoundWhenIdDoesNotExist() throws Exception {

		// converte o objecto em string..
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		ResultActions result = mockMvc.perform(put("/products/{id}", nonExistingId)
				.content(jsonBody) // pega o conteudo de object em JSON
				.contentType(MediaType.APPLICATION_JSON) //O tipo de conteudo é JSON
				.accept(MediaType.APPLICATION_JSON)); // O tipo de dados é JSON.

		result.andExpect(status().isNotFound());
	}

}
