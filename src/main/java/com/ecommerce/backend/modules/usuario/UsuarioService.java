package com.ecommerce.backend.modules.usuario;

import com.ecommerce.backend.modules.usuario.dto.UsuarioDTO;
import com.ecommerce.backend.modules.usuario.dto.UsuarioUpdateRequest;
import com.ecommerce.backend.shared.exception.BadRequestException;
import com.ecommerce.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;

    // ── Obtener usuario por ID ───────────────────────────────
    public UsuarioDTO obtenerPorId(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        return mapearADTO(usuario);
    }

    // ── Obtener usuario por correo ───────────────────────────
    public UsuarioDTO obtenerPorCorreo(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "correo", correo));
        return mapearADTO(usuario);
    }

    // ── Listar todos los usuarios (admin) ────────────────────
    public List<UsuarioDTO> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    // ── Actualizar perfil ────────────────────────────────────
    @Transactional
    public UsuarioDTO actualizarPerfil(Integer id, UsuarioUpdateRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        if (request.getNombre() != null)
            usuario.setNombre(request.getNombre());
        if (request.getApellido() != null)
            usuario.setApellido(request.getApellido());
        if (request.getTelefono() != null)
            usuario.setTelefono(request.getTelefono());
        if (request.getAvatarUrl() != null)
            usuario.setAvatarUrl(request.getAvatarUrl());

        return mapearADTO(usuarioRepository.save(usuario));
    }

    // ── Desactivar usuario (admin) ───────────────────────────
    @Transactional
    public void desactivarUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        if (!usuario.getEstaActivo()) {
            throw new BadRequestException("El usuario ya está desactivado");
        }

        usuario.setEstaActivo(false);
        usuarioRepository.save(usuario);
    }

    // ── Activar usuario (admin) ──────────────────────────────
    @Transactional
    public void activarUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        if (usuario.getEstaActivo()) {
            throw new BadRequestException("El usuario ya está activo");
        }

        usuario.setEstaActivo(true);
        usuarioRepository.save(usuario);
    }

    // ── Convertir entidad a DTO ──────────────────────────────
    public UsuarioDTO mapearADTO(Usuario usuario) {
        UsuarioDTO dto = modelMapper.map(usuario, UsuarioDTO.class);
        dto.setNombreCompleto(usuario.getNombreCompleto());
        dto.setRoles(usuario.getRoles()
                .stream()
                .map(rol -> rol.getNombre().name())
                .collect(Collectors.toSet()));
        return dto;
    }
}