package com.ecommerce.backend.modules.auth;

import com.ecommerce.backend.modules.auth.dto.AuthResponse;
import com.ecommerce.backend.modules.auth.dto.LoginRequest;
import com.ecommerce.backend.modules.auth.dto.RegisterRequest;
import com.ecommerce.backend.modules.usuario.Rol;
import com.ecommerce.backend.modules.usuario.RolRepository;
import com.ecommerce.backend.modules.usuario.Usuario;
import com.ecommerce.backend.modules.usuario.UsuarioRepository;
import com.ecommerce.backend.modules.usuario.UsuarioService;
import com.ecommerce.backend.security.JwtUtil;
import com.ecommerce.backend.shared.enums.RolNombre;
import com.ecommerce.backend.shared.exception.BadRequestException;
import com.ecommerce.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

        private final UsuarioRepository usuarioRepository;
        private final RolRepository rolRepository;
        private final UsuarioService usuarioService;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtil jwtUtil;
        private final AuthenticationManager authenticationManager;
        private final UserDetailsService userDetailsService;

        // ── REGISTRO COMPRADOR ───────────────────────────────────
        @Transactional
        public AuthResponse registrar(RegisterRequest request) {

                if (usuarioRepository.existsByCorreo(request.getCorreo())) {
                        throw new BadRequestException("El correo ya está registrado");
                }

                Rol rolCliente = rolRepository.findByNombre(RolNombre.COMPRADOR)
                                .orElseThrow(() -> new ResourceNotFoundException("Rol COMPRADOR no encontrado"));

                Usuario usuario = Usuario.builder()
                                .nombre(request.getNombre())
                                .apellido(request.getApellido())
                                .correo(request.getCorreo())
                                .contrasenaHash(passwordEncoder.encode(request.getContrasena()))
                                .telefono(request.getTelefono())
                                .estaActivo(true)
                                .build();

                usuario.agregarRol(rolCliente);
                usuarioRepository.save(usuario);

                UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getCorreo());
                String accessToken = jwtUtil.generarToken(userDetails);
                String refreshToken = jwtUtil.generarRefreshToken(userDetails);

                return AuthResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .usuario(usuarioService.mapearADTO(usuario))
                                .build();
        }

        // ── REGISTRO VENDEDOR ────────────────────────────────────
        @Transactional
        public void registrarVendedor(RegisterRequest request) {

                if (usuarioRepository.existsByCorreo(request.getCorreo())) {
                        throw new BadRequestException("El correo ya está registrado");
                }

                Rol rolVendedor = rolRepository.findByNombre(RolNombre.VENDEDOR)
                                .orElseThrow(() -> new ResourceNotFoundException("Rol VENDEDOR no encontrado"));

                Usuario usuario = Usuario.builder()
                                .nombre(request.getNombre())
                                .apellido(request.getApellido())
                                .correo(request.getCorreo())
                                .contrasenaHash(passwordEncoder.encode(request.getContrasena()))
                                .telefono(request.getTelefono())
                                .estaActivo(true)
                                .build();

                usuario.agregarRol(rolVendedor);
                usuarioRepository.save(usuario);
                // No genera token — pendiente de aprobación del admin
        }

        // ── LOGIN ────────────────────────────────────────────────
        public AuthResponse login(LoginRequest request) {

                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getCorreo(),
                                                request.getContrasena()));

                UserDetails userDetails = userDetailsService.loadUserByUsername(request.getCorreo());

                Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "correo",
                                                request.getCorreo()));

                String accessToken = jwtUtil.generarToken(userDetails);
                String refreshToken = jwtUtil.generarRefreshToken(userDetails);

                return AuthResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .usuario(usuarioService.mapearADTO(usuario))
                                .build();
        }

        // ── REFRESH TOKEN ────────────────────────────────────────
        public AuthResponse refreshToken(String refreshToken) {

                String correo = jwtUtil.extraerCorreo(refreshToken);

                UserDetails userDetails = userDetailsService.loadUserByUsername(correo);

                if (!jwtUtil.esTokenValido(refreshToken, userDetails)) {
                        throw new BadRequestException("Refresh token inválido o expirado");
                }

                Usuario usuario = usuarioRepository.findByCorreo(correo)
                                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "correo", correo));

                String nuevoAccessToken = jwtUtil.generarToken(userDetails);

                return AuthResponse.builder()
                                .accessToken(nuevoAccessToken)
                                .refreshToken(refreshToken)
                                .usuario(usuarioService.mapearADTO(usuario))
                                .build();
        }

        // ── CREAR ADMIN ──────────────────────────────────────────
        @Transactional
        public AuthResponse crearAdmin(RegisterRequest request) {

                if (usuarioRepository.existsByCorreo(request.getCorreo())) {
                        throw new BadRequestException("El correo ya está registrado");
                }

                Rol rolAdmin = rolRepository.findByNombre(RolNombre.ADMIN)
                                .orElseThrow(() -> new ResourceNotFoundException("Rol ADMIN no encontrado"));

                Usuario usuario = Usuario.builder()
                                .nombre(request.getNombre())
                                .apellido(request.getApellido())
                                .correo(request.getCorreo())
                                .contrasenaHash(passwordEncoder.encode(request.getContrasena()))
                                .telefono(request.getTelefono())
                                .estaActivo(true)
                                .build();

                usuario.agregarRol(rolAdmin);
                usuarioRepository.save(usuario);

                UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getCorreo());
                String accessToken = jwtUtil.generarToken(userDetails);
                String refreshToken = jwtUtil.generarRefreshToken(userDetails);

                return AuthResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .usuario(usuarioService.mapearADTO(usuario))
                                .build();
        }
}