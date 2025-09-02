package ch.so.agi.modelfinder;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import gg.jte.generated.precompiled.JteindexGenerated;
import gg.jte.generated.precompiled.JtemodelmetadataGenerated;
import gg.jte.generated.precompiled.JtemodelsGenerated;
import gg.jte.generated.precompiled.JtesearchresultsGenerated;
import gg.jte.generated.precompiled.JteumlGenerated;

public class ResourceRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.resources()
            .registerPattern("**/*.bin");
        
        hints.reflection()
            .registerType(JteindexGenerated.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(JteumlGenerated.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(JtemodelmetadataGenerated.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(JtemodelsGenerated.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(JtesearchresultsGenerated.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS);
    }
}
