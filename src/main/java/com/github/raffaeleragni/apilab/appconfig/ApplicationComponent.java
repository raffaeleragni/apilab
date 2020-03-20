package com.github.raffaeleragni.apilab.appconfig;

import dagger.Component;
import javax.inject.Singleton;

@Component(modules = {ApplicationConfig.class})
@Singleton
public interface ApplicationComponent {
  Application application();
}
