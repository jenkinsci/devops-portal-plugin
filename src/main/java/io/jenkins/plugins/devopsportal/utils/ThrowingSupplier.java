package io.jenkins.plugins.devopsportal.utils;

/**
 * Functional interface for Suppliers throwing exceptions
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
@FunctionalInterface
public interface ThrowingSupplier<T, E extends Exception> {
    T get() throws E;
}
