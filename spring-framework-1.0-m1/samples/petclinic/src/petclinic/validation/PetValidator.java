/*
 * PetValidator.java
 *
 */

package petclinic.validation;

import petclinic.Pet;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 *  JavaBean <code>Validator</code> for <code>Pet</code> Forms.
 *
 *  @author  Ken Krebs
 */
public class PetValidator implements Validator {

	public boolean supports(Class clazz) {
		return clazz.equals(Pet.class);
	}

	public void validate(Object obj, Errors errors) {
		Pet pet = (Pet) obj;
		String name = pet.getName();
		if (name == null || "".equals(name)) {
			errors.rejectValue("name", "required", null, "required");
		}
		if (pet.getId() == 0 && pet.getOwner().hasPet(name)) {
			errors.rejectValue("name", "duplicate", null, "already exists");
		}
	}

}
