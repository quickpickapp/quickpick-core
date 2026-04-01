package de.lukasbreuer.quickpick.core.configuration;

import org.apache.commons.configuration2.AbstractConfiguration;

public interface Configuration {
  /**
   * This function builds the actual objects from the content of the
   * configuration file, which can be used later on
   * @param file The configuration file
   */
  void load(AbstractConfiguration file);
}
