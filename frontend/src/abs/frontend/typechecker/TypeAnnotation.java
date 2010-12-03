package abs.frontend.typechecker;

import abs.frontend.ast.Annotation;
import abs.frontend.ast.PureExp;

public class TypeAnnotation {
    private DataTypeType type;
    private PureExp value;
    
    public TypeAnnotation(Annotation a) {
        assert a.getType() instanceof DataTypeType;
        value = a.getValue();
        type = (DataTypeType) a.getType();
    }
    
    public DataTypeType getType() {
        return type;
    }
    
    public PureExp getValue() {
        return value;
    }

}
