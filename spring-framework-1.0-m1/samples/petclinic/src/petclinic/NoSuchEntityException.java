/*
 * NoSuchEntityException.java
 *
 */

package petclinic;

import org.springframework.core.NestedRuntimeException;

/**
 *  Exception thrown when an id reference is made to an 
 *  <code>Entity</code> that should be in the system but is not.
 *
 *  @author  Ken Krebs
 */
public class NoSuchEntityException extends NestedRuntimeException {
    
    /**
     * Creates a new instance of NoSuchIdException.
     */
    public NoSuchEntityException(Entity entity) {
		super("No such " + entity.getClass().getName() + " with id " + entity.getId());
    }
    
}
