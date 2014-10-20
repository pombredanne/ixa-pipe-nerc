/*
 *  Copyright 2014 Rodrigo Agerri

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package es.ehu.si.ixa.pipe.nerc.features;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;
import opennlp.tools.util.featuregen.ArtifactToSerializerMapper;
import opennlp.tools.util.featuregen.CustomFeatureGenerator;
import opennlp.tools.util.featuregen.FeatureGeneratorResourceProvider;
import opennlp.tools.util.model.ArtifactSerializer;
import es.ehu.si.ixa.pipe.nerc.dict.Dictionary;
import es.ehu.si.ixa.pipe.nerc.train.InputOutputUtils;


public class DictionaryFeatureGenerator extends CustomFeatureGenerator implements  ArtifactToSerializerMapper {

  private String[] currentSentence;
  private Span[] currentNames;
  private Dictionary dictionary;
  private Map<String, String> attributes;
  
  public DictionaryFeatureGenerator() {
  }
  
  //TODO: consider generation start and continuation features
  public void createFeatures(List<String> features, String[] tokens, int index, String[] previousOutcomes) {
    
    DictionaryFeatureFinder finder = new DictionaryFeatureFinder(dictionary);
    // cache results for sentence
    if (currentSentence != tokens) {
      currentSentence = tokens;
      currentNames = finder.nercToSpans(tokens);
    }
    // iterate over names and check if a span is contained
    for (int i = 0; i < currentNames.length; i++) {
      if (currentNames[i].contains(index)) {
        features.add(attributes.get("dict") + ":w=dict");
        features.add(attributes.get("dict") + ":w=dict=" + tokens[index]);
        break;
      }
    }
  }
  
  @Override
  public void updateAdaptiveData(String[] tokens, String[] outcomes) {
  }
  @Override
  public void clearAdaptiveData() {
  }
  @Override
  public void init(Map<String, String> properties,
      FeatureGeneratorResourceProvider resourceProvider)
      throws InvalidFormatException {
    this.attributes = properties;
    InputStream inputStream = InputOutputUtils.getDictionaryResource("en/dictionaries/" + properties.get("dict"));
    try {
      this.dictionary = new Dictionary.DictionarySerializer().create(inputStream);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public Map<String, ArtifactSerializer<?>> getArtifactSerializerMapping() {
    Map<String, ArtifactSerializer<?>> mapping = new HashMap<>();
    mapping.put(attributes.get("dict"), new Dictionary.DictionarySerializer());
    return Collections.unmodifiableMap(mapping);
  }
  
  
  
}
