package de.hub.mse.emf.multifile.base.fuzz;

import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public class JQFF extends JQF {

    public JQFF(Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    @Override public Statement methodBlock(FrameworkMethod method) {
        if (method.getAnnotation(Fuzz.class) != null) {
            return new ModelFuzzStatement(method, getTestClass(), this.generatorRepository);
        }
        return super.methodBlock(method);
    }
}