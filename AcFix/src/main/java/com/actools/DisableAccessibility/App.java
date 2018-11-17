package fr.inria.DisableAccessibility;


import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.code.CtLocalVariableImpl;
import spoon.support.reflect.code.CtVariableReadImpl;
import spoon.support.reflect.declaration.CtFieldImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class App {
    private static final String CODE_PATH = "/Users/mnaseri/Desktop/Inria/Project/testAccessiblity/app/src/main/java";
    private static final String ANDROID_JAR_PATH = "/Users/mnaseri/Library/Android/sdk/platforms/android-25/android.jar";
    private static final String EDIT_TEXT_PACKAGE = "android.support.v7.widget.AppCompatEditText";//"android.widget.EditText";
    private static final String COMPAT_PACKAGE = "/Users/mnaseri/Library/Android/sdk/extras/android/m2repository/com/android/support/appcompat-v7/25.3.1/appcompat-v7-25.3.1-sources.jar";
    private static final String FIND_VIEW_METHOD = "findViewById";
    private static final String SET_ACCESS_METHOD = "setImportantForAccessibility";
    public static Launcher launcher;
    public static CtTypeReference androidEditTextReference;
    public static Set<String> editTextIds = new HashSet<>();

    public static void main(String[] args) {

        try {
            StoreAnalyzer.setup();
            StoreAnalyzer.fetchPermissions();

        } catch (Exception e){

            System.out.println(e.getMessage());
        }





//        launcher = new Launcher();
//        launcher.getEnvironment().setShouldCompile(true);
//        launcher.getEnvironment().setNoClasspath(true);
//        launcher.addInputResource(CODE_PATH);
//        launcher.getEnvironment().setSourceClasspath(new String[]{ANDROID_JAR_PATH, COMPAT_PACKAGE});
//        launcher.setBinaryOutputDirectory(ANDROID_JAR_PATH);
//        launcher.run();
//
//        androidEditTextReference = launcher.getFactory().Type().createReference(EDIT_TEXT_PACKAGE);
//
//        ResourceProcessor finder = new ResourceProcessor();
////        finder.process();
//
//        CtModel model = launcher.getModel();
//        for (CtType<?> classInstance : model.getAllTypes()) {
//            proClass(classInstance);
//        }
//        ResourceProcessor.enforceChanges();
    }

    private static void proClass(CtType<?> s) {
        List<CtVariable> varList = s.getElements(new TypeFilter(CtVariable.class));
        List<CtVariable> targetList = new ArrayList<>();
        for (CtVariable variable : varList) {
            if (!(variable instanceof CtParameter)) {
                if (variable.getAnnotations().size() != 0) {
                    //it is annotation
                    for (CtAnnotation annotation : variable.getAnnotations()) {
                        if (annotation.getType().getSimpleName().equals("BindView") &&
                                annotation.getParent().getElements(new TypeFilter<>(CtFieldImpl.class)).get(0).getType().getSimpleName().equals("EditText")
                                ) {
//                            && ResourceProcessor.resources.containsKey(getId(annotation.getValue("value").toString()))) {
                            targetList.add(variable);
                        }
                    }
                } else {
                    List<CtInvocation> invocations = variable.getElements(new TypeFilter(CtInvocation.class));
                    if (invocations.size() != 0 && invocations.get(0).getExecutable().getSimpleName().equals(FIND_VIEW_METHOD)
                            && invocations.get(0).getTypeCasts().get(0).toString().equals("android.widget.EditText")) {
                        targetList.add(variable);
                    }
                }
            }
        }

        List<CtInvocation> invocations = s.getElements(new TypeFilter(CtInvocation.class));
        for (CtInvocation<?> invocation : invocations) {
            if (!targetList.isEmpty() && invocation.getExecutable().getSimpleName().equals(SET_ACCESS_METHOD) &&
                    (invocation.getArguments().get(0).toString().equals("android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO") ||
                            invocation.getArguments().get(0).toString().equals("android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS"))) {
                if (invocation.getTarget() instanceof CtFieldRead) {
                    CtVariable tempVar = ((CtFieldRead) ((CtInvocationImpl<?>) invocation).getTarget()).getVariable().getDeclaration();
                    targetList.remove(tempVar);
                }
                if (invocation.getTarget() instanceof CtVariableReadImpl) {
                    CtVariable tempVar = ((CtVariableReadImpl) ((CtInvocationImpl<?>) invocation).getTarget()).getVariable().getDeclaration();
                    targetList.remove(tempVar);
                }
            }
        }

        if (targetList.size() != 0) {
            extractIds(targetList);
        }
    }

    private static void extractIds(List<CtVariable> list) {
        for (CtVariable variable : list) {
            if (variable.getAnnotations().size() != 0) {
                String editTextId = getId(variable.getAnnotations().get(0).getValue("value").toString());
                editTextIds.add(editTextId);

            } else {
                String temp = ((CtLocalVariableImpl) variable).getAssignment().toString();
                String editTextId = temp.substring(temp.indexOf("R.id.") + 5).replace(")))", "");
                editTextIds.add(editTextId);
            }
        }
    }

    private static String getId(String fullId) {
        return fullId.replace("R.id.", "");
    }

    private static boolean isSubTypeOfEditText(CtElement element) {
        return true;
    }
}
