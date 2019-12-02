package com.openwebstart.proxy.util.pac;

public class SimplePacBasedProvider extends AbstractPacBasedProvider {

    private final PacFileEvaluator pacEvaluator;

    public SimplePacBasedProvider(final PacFileEvaluator pacEvaluator) {
        this.pacEvaluator = pacEvaluator;
    }

    @Override
    protected PacFileEvaluator getPacEvaluator() {
        return pacEvaluator;
    }
}
