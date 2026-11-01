package com.hadiyarajesh.kmptemplate.di

import dev.zacsweers.metrox.viewmodel.ViewModelGraph

/**
 * The only graph type that leaves this module. Bindings are merged in through
 * `@ContributesBinding`, `@ContributesIntoMap` and `@ContributesTo`, so graphs stay empty.
 */
public interface CommonAppGraph : ViewModelGraph
