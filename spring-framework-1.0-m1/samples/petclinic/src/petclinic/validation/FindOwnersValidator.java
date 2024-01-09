/*
 * FindOwnersValidator.java
 *
 */

package petclinic.validation;

import petclinic.Owner;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 *  JavaBean <code>Validator</code> for <code>FindOwnerForm</code>.
 *
 *  @author  Ken Krebs
 */
public class FindOwnersValidator implements Validator {
    
    public boolean supports(Class clazz) {
        return clazz.equals(Owner.class);
    }
    
    public void validate(Object obj, Errors errors) {
		Owner owner = (Owner) obj;
		String lastName = owner.getLastName();
        if(lastName == null || "".equals(lastName)) {
            errors.rejectValue("lastName", "required", null, "required");
        }
    }
    
}
