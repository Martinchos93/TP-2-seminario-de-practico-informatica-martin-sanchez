package ar.edu.siglo21.tribu.web;

import ar.edu.siglo21.tribu.service.ReglaNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class ManejadorDeErrores {
    private static final Logger log = LoggerFactory.getLogger(ManejadorDeErrores.class);

    @ExceptionHandler(ReglaNegocioException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView reglaDeNegocio(ReglaNegocioException e, HttpServletRequest request) {
        log.warn("Regla de negocio incumplida en {}: {}", request.getRequestURI(), e.getMessage());

        ModelAndView vista = new ModelAndView("error/regla-negocio");
        vista.addObject("mensaje", e.getMessage());
        return vista;
    }
}
