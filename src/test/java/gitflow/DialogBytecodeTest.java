package gitflow;

import org.junit.Test;

import java.io.InputStream;
import java.lang.classfile.ClassFile;
import java.lang.classfile.MethodModel;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DialogBytecodeTest {
    @Test
    public void formClassesHaveNoDuplicateMethods() throws Exception {
        for (String className : new String[]{
                "AbstractBranchStartDialog", "AbstractGitflowActionAckDialog", "GitflowBranchChooseDialog",
                "GitflowCloseTaskPanel", "GitflowInitOptionsDialog", "GitflowOpenTaskPanel", "GitflowOptionsForm"
        }) {
            assertNoDuplicateMethods(className);
        }
    }

    private void assertNoDuplicateMethods(String className) throws Exception {
        try (InputStream input = getClass().getResourceAsStream("/gitflow/ui/" + className + ".class")) {
            assertNotNull(input);
            Set<String> signatures = new HashSet<>();
            for (MethodModel method : ClassFile.of().parse(input.readAllBytes()).methods()) {
                String signature = method.methodName().stringValue() + method.methodType().stringValue();
                assertTrue("Duplicate method in " + className + ": " + signature, signatures.add(signature));
            }
        }
    }
}
