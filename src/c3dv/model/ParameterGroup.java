package c3dv.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A collection of parameters.
 * 
 * @author Justin Stoecker
 */
public class ParameterGroup {

  List<Parameter>        parameters = new ArrayList<Parameter>();
  Map<String, Parameter> paramMap   = new HashMap<String, Parameter>();
  String                 name;
  String                 description;
  int                    id;
  boolean                locked;

  public ParameterGroup(String name, int id, String description, boolean locked) {
    this.name = name;
    this.id = id;
    this.description = description;
    this.locked = locked;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public int getId() {
    return id;
  }

  public List<Parameter> getParameters() {
    return parameters;
  }

  public void addParameter(Parameter parameter) {
    parameters.add(parameter);
    paramMap.put(parameter.name, parameter);
  }

  public Parameter getParameter(String name) {
    return paramMap.get(name);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Parameter Group:");
    sb.append("\nName : " + name);
    sb.append("\nID : " + id);
    sb.append("\nDescription : " + description);
    return sb.toString();
  }
}
