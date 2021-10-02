package pt.amane.dscatalog.resources;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import pt.amane.dscatalog.dtos.ProductDTO;
import pt.amane.dscatalog.services.ProductService;
import pt.amane.dscatalog.services.exceptions.DataBaseIntegrityViolationException;
import pt.amane.dscatalog.services.exceptions.ResourceNotFoundException;
import pt.amane.dscatalog.tests.Factory;

@WebMvcTest(ProductResource.class)
class ProductResourseTests {

	@Autowired
	private MockMvc mockMvc;

	// essa anotação é aceite pq o ObjectMapper ele auxilia o objeto
	// ele nao e a dependencia de produtoresource ou nao interfere em componente..
	@Autowired
	private ObjectMapper objectMapper;

	private ProductDTO productDTO;

	// PageImpl é um objecto concreto e aceita o new.
	// é perferivel usa-lo do que Page.
	private PageImpl<ProductDTO> page;

	private Long existingId;
	private Long nonExistingId;
	private Long dependentId;

	@MockBean
	ProductService service;

	@BeforeEach // vai ser executado 1º antes de qualquer teste.
	void setUp() throws Exception {

		// esses valores são simulado e nao mexe com o banco..
		existingId = 1L;
		nonExistingId = 2L;
		dependentId = 3L;

		productDTO = Factory.createProductDTO();
		page = new PageImpl<>(List.of(productDTO));

		/**
		 * import static org.mockito.Mockito.*; tem varios metodos como: when, any(),
		 * eq, doNothing, doThrow ou se quiser pose fazer import statico pacada um deles, e remove o
		 * ArgumentMathers em cada um deles. como: import static
		 * org.mockito.Mockito.when; import static org.mockito.Mockito.any; import
		 * static org.mockito.Mockito.eq;
		 */

		when(service.findAllPaged(ArgumentMatchers.any())).thenReturn(page);

		when(service.findById(existingId)).thenReturn(productDTO);
		when(service.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);
		
		when(service.create(ArgumentMatchers.any())).thenReturn(productDTO);

		when(service.update(eq(existingId), ArgumentMatchers.any())).thenReturn(productDTO);
		when(service.update(eq(nonExistingId), ArgumentMatchers.any())).thenThrow(ResourceNotFoundException.class);
		
		doNothing().when(service).delete(existingId);
		doThrow(ResourceNotFoundException.class).when(service).delete(nonExistingId);
		doThrow(DataBaseIntegrityViolationException.class).when(service).delete(dependentId);
	}

	/**
	 * o metodo mockMvc.perform(get("/products")); pode ser do tipo post, put
	 * delete, etc. esse verbo vem qd esta testar o web e passar o caminho nesse
	 * caso o endpoint é products. E tambem, em vez de fazer só numa linha:
	 * mockMvc.perform(get("/products")).andExpect(status().isOk()); para min esse
	 * de ima é mais claro.. pode tambem fazer dessa forma: ResultActions result =
	 * mockMvc.perform(get("/products") .accept(MediaType.APPLICATION_JSON)); onde o
	 * ResultActions recebe a chamada de requisição do tipo get nesse caso q pode
	 * ser (post, put, delete, patch). .accept(MediaType.APPLICATION_JSON) informa
	 * para http q o tipo de retorno é um JSON. result.andExpect(status().isOk()); é
	 * a chamada de assertions.
	 * 
	 * @throws Exception
	 */
	@Test
	void findAllShouldReturnPage() throws Exception {
		ResultActions result = mockMvc.perform(get("/products").accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isOk());
	}

	@Test
	void findByIdShouldReturnProductWhenIdExists() throws Exception {

		ResultActions result = mockMvc.perform(get("/products/{id}", existingId).accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isOk());
		// esse metodo testa se existe esse atributo json.
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
		result.andExpect(jsonPath("$.description").exists());
	}

	@Test
	void findByIdShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {

		ResultActions result = mockMvc.perform(get("/products/{id}", nonExistingId).accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isNotFound());
	}

	@Test
	void updateShoudReturnProductDTOWhenIdExists() throws Exception {

		// converte o objecto em string..
		String jsonBody = objectMapper.writeValueAsString(productDTO);

		ResultActions result = mockMvc.perform(put("/products/{id}", existingId)
				.content(jsonBody) // pega o conteudo JSON
				.contentType(MediaType.APPLICATION_JSON) // o tipo de conteudo é JSON
				.accept(MediaType.APPLICATION_JSON)); // o tipo de dados é JSON.

		result.andExpect(status().isOk());
		// esse metodo testa se existe esse atributo json.
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
		result.andExpect(jsonPath("$.description").exists());

	}

	@Test
	void updateShoudReturnNotFoundWhenIdDoesNotExist() throws Exception {

		// converte o objecto em string..
		String jsonBody = objectMapper.writeValueAsString(productDTO);

		ResultActions result = mockMvc.perform(put("/products/{id}", nonExistingId)
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isNotFound());
	}
	
	
	@Test
	void createShoudReturnProductDTOCreated() throws Exception {

		// converte o objecto em string..
		String jsonBody = objectMapper.writeValueAsString(productDTO);

		ResultActions result = mockMvc.perform(post("/products")
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isCreated());
		// esse metodo testa se existe esse atributo json.
		//result.andExpect(jsonPath("$.id").exists());
		//result.andExpect(jsonPath("$.name").exists());
		//result.andExpect(jsonPath("$.description").exists());

	}
	
	@Test
	void deleteShoudReturnNoContentWhenIdExists() throws Exception {
		
		ResultActions result = mockMvc.perform(delete("/products/{id}", existingId)
				.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isNoContent());
	}
	
	@Test
	void deleteShoudReturnNoFoundWhenIdDoesNotExist() throws Exception {
		
		ResultActions result = mockMvc.perform(delete("/products/{id}", nonExistingId)
				.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isNotFound());
	}

}
