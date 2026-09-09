package br.com.yourpethealth.exception;

import br.com.yourpethealth.dto.response.ApiErroResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice(basePackages = "br.com.yourpethealth.controller.api")
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IdNaoEncontradoException.class)
    public ResponseEntity<ApiErroResponse> naoEncontrado(
            IdNaoEncontradoException ex, HttpServletRequest req) {
        return montar(HttpStatus.NOT_FOUND, "NAO_ENCONTRADO", ex.getMessage(), req, null);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ApiErroResponse> regraNegocio(
            RegraNegocioException ex, HttpServletRequest req) {
        return montar(HttpStatus.CONFLICT, "CONFLITO", ex.getMessage(), req, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErroResponse> validacao(
            MethodArgumentNotValidException ex, HttpServletRequest req) {

        List<ApiErroResponse.CampoErro> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ApiErroResponse.CampoErro(e.getField(), e.getDefaultMessage()))
                .toList();

        return montar(HttpStatus.BAD_REQUEST, "VALIDACAO",
                "Há campos inválidos na requisição", req, campos);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErroResponse> constraint(
            ConstraintViolationException ex, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST, "VALIDACAO", ex.getMessage(), req, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErroResponse> corpoIlegivel(
            HttpMessageNotReadableException ex, HttpServletRequest req) {
        log.debug("Corpo da requisição ilegível", ex);
        return montar(HttpStatus.BAD_REQUEST, "VALIDACAO",
                "Corpo da requisição inválido ou mal formatado", req, null);
    }

    @ExceptionHandler({BadCredentialsException.class, DisabledException.class})
    public ResponseEntity<ApiErroResponse> credenciais(HttpServletRequest req) {
        // Mensagem genérica: não revelar se o e-mail existe.
        return montar(HttpStatus.UNAUTHORIZED, "NAO_AUTORIZADO",
                "E-mail ou senha inválidos", req, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErroResponse> acessoNegado(HttpServletRequest req) {
        return montar(HttpStatus.FORBIDDEN, "ACESSO_NEGADO",
                "Você não tem permissão para acessar este recurso", req, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErroResponse> integridade(
            DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("Violação de integridade em {}", req.getRequestURI(), ex);
        return montar(HttpStatus.CONFLICT, "CONFLITO",
                "Já existe um registro com esses dados ou há relacionamento inválido",
                req, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErroResponse> mediaType(HttpServletRequest req) {
        return montar(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "VALIDACAO",
                "Utilize Content-Type: application/json", req, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErroResponse> metodo(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return montar(HttpStatus.METHOD_NOT_ALLOWED, "VALIDACAO", ex.getMessage(), req, null);
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ApiErroResponse> pathAusente(
            MissingPathVariableException ex, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST, "VALIDACAO", ex.getMessage(), req, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErroResponse> generico(Exception ex, HttpServletRequest req) {
        // Detalhe só no log: a mensagem pode conter nome de tabela, coluna ou SQL.
        log.error("Erro não tratado em {}", req.getRequestURI(), ex);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO",
                "Erro interno do servidor", req, null);
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ApiErroResponse> acessoNegadoDominio(
            AcessoNegadoException ex, HttpServletRequest req) {
        return montar(HttpStatus.FORBIDDEN, "ACESSO_NEGADO", ex.getMessage(), req, null);
    }

    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<ApiErroResponse> validacaoDominio(
            ValidacaoException ex, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST, "VALIDACAO", ex.getMessage(), req, null);
    }

    private ResponseEntity<ApiErroResponse> montar(
            HttpStatus status, String codigo, String mensagem,
            HttpServletRequest req, List<ApiErroResponse.CampoErro> campos) {

        return ResponseEntity.status(status).body(new ApiErroResponse(
                LocalDateTime.now(), status.value(), codigo,
                mensagem, req.getRequestURI(), campos));
    }
}