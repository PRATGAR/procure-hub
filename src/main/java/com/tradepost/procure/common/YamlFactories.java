package com.tradepost.procure.common;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public final class YamlFactories {

    private YamlFactories() {
    }

    public static Yaml ruleFiles() {
        return new Yaml(new SafeConstructor(new LoaderOptions()));
    }
}
