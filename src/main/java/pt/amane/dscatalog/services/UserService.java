package pt.amane.dscatalog.services;

import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.amane.dscatalog.dtos.RoleDTO;
import pt.amane.dscatalog.dtos.UserDTO;
import pt.amane.dscatalog.dtos.UserInsertDTO;
import pt.amane.dscatalog.dtos.UserUpdateDTO;
import pt.amane.dscatalog.entities.Role;
import pt.amane.dscatalog.entities.User;
import pt.amane.dscatalog.repositories.RoleRepository;
import pt.amane.dscatalog.repositories.UserRepository;
import pt.amane.dscatalog.services.exceptions.DataBaseIntegrityViolationException;
import pt.amane.dscatalog.services.exceptions.ResourceNotFoundException;

@Service
public class UserService implements UserDetailsService{

	private Logger logger = LoggerFactory.getLogger(UserService.class);
	
	@Autowired
	private UserRepository repository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private AuthService authService;
	
	@Transactional(readOnly = true)
	public UserDTO findById(Long id) {
		// verifica se user esta logado ou nao ou se é admin
		authService.validateSelfOrAdmin(id);
				
		Optional<User> userId = repository.findById(id);
		User user = userId.orElseThrow(() -> new ResourceNotFoundException(
				"Object not found! Id: " + id + ", Type: " + UserDTO.class.getName()));
		return new UserDTO(user);
	}

	@Transactional(readOnly = true)
	public Page<UserDTO> findAllPaged(Pageable pageable) {
		Page<User> products = repository.findAll(pageable);
		return products.map(dto -> new UserDTO(dto));
	}

	@Transactional
	public UserDTO create(UserInsertDTO dto) {
		User user = new User();
		copyDtoToUsery(dto, user);
		user.setPassword(passwordEncoder.encode(dto.getPassword()));
		user = repository.save(user);
		return new UserDTO(user);
	}

	@Transactional
	public UserDTO update(Long id, UserUpdateDTO dto) {
		try {
			User user = repository.getOne(id);
			copyDtoToUsery(dto, user);
			user = repository.save(user);
			return new UserDTO(user);
		} catch (EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id not found! Id: " + id + ", Type: " + UserDTO.class.getName());
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

	private void copyDtoToUsery(UserDTO dto, User user) {

		user.setFirstName(dto.getFirstName());
		user.setLastName(dto.getLastName());
		user.setEmail(dto.getEmail());
		
		user.getRoles().clear();
		for(RoleDTO roleDTO: dto.getRoleDTOs()) {
			Role role = roleRepository.getOne(roleDTO.getId());
			user.getRoles().add(role);
		}
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		User user = repository.findByEmail(username);
		if(user == null) {
			logger.error("Email not found! " + username);
			throw new UsernameNotFoundException("Email not found!");
		}
		logger.info("User found success! " + username);
		return user;
	}

}
