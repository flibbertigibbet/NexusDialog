package com.github.dkharrat.nexusdialog.validations;

import org.hibernate.validator.HibernateValidator;

import java.util.Collections;
import java.util.List;

import javax.validation.Validation;
import javax.validation.ValidationProviderResolver;
import javax.validation.Validator;
import javax.validation.spi.ValidationProvider;

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
                .buildValidatorFactory().getValidator();
    }
}
