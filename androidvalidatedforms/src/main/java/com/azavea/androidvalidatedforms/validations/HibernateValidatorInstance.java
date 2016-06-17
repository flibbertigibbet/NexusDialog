package com.azavea.androidvalidatedforms.validations;

import android.app.Application;
import android.util.Log;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.validation.Validation;
import javax.validation.ValidationProviderResolver;
import javax.validation.Validator;
import javax.validation.spi.ValidationProvider;

import dalvik.system.PathClassLoader;

/**
 * Share a single Hibernate Validator instance across the app.
 *
 * Created by kathrynkillebrew on 12/11/15.
 */
public class HibernateValidatorInstance {

    private static Validator validator = createValidator();

    /**
     * Get validator instance for the app
     * @return Hibernate validator
     */
    public static Validator getValidator() {
        return validator;
    }

    private static Validator createValidator() {

        // Define a resource bundle that will find the appropriate ValidationMessages_<locale>.properties
        // file on the classpath.
        PlatformResourceBundleLocator resourceBundleLocator =
                new PlatformResourceBundleLocator(ResourceBundleMessageInterpolator.USER_VALIDATION_MESSAGES);

        return Validation
                .byProvider(HibernateValidator.class)
                .providerResolver(new ValidationProviderResolver() {
                    @Override
                    public List<ValidationProvider<?>> getValidationProviders() {
                        return Collections.<ValidationProvider<?>>singletonList(new HibernateValidator());
                    }
                })
                .configure()
                .ignoreXmlConfiguration()
                .messageInterpolator(new ResourceBundleMessageInterpolator(resourceBundleLocator))
                .buildValidatorFactory().getValidator();
    }
}
