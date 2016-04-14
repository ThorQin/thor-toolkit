/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.thorqin.toolkit.validation;

import com.github.thorqin.toolkit.utility.Localization;

/**
 *
 * @author nuo.qin
 */
public interface Validatable {
	/**
	 * If object do not pass the validation then throws ValidateException.
     * @param loc Provide locale info for generate validation error message
	 * @throws com.github.thorqin.toolkit.validation.ValidateException If not pass the validation then throw ValidateException
	 */
	void validate(Localization loc) throws ValidateException;
}
