package de.interoberlin.poisondartfrog.model.mapping.actions;

import de.interoberlin.poisondartfrog.model.mapping.Sink;

public interface IAction {
    void perform(Sink sink);
}
