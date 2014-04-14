// Copyright 2014 Connectifier, Inc. All Rights Reserved.

package com.connectifier.xeroclient.jibx;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleType;
import org.jibx.binding.model.ElementBase;
import org.jibx.schema.codegen.IClassHolder;
import org.jibx.schema.codegen.extend.ClassDecorator;

public class CodeRewriter implements ClassDecorator {

  private static final Map<String,String> wrappedTypeOverrides = new HashMap<>();
  static {
    wrappedTypeOverrides.put("ArrayOfTrackingCategoryOption", "optionList");
    wrappedTypeOverrides.put("ArrayOfManualJournalLine", "journalLineList");
  }
  
  @Override
  public void start(IClassHolder holder) {
    holder.addImport("java.util.List");
  }

  @Override
  public void valueAdded(String basename, boolean collect, String type, FieldDeclaration field,
      MethodDeclaration getmeth, MethodDeclaration setmeth, String descript, IClassHolder holder) {
    AST ast = field.getAST();
    String typeSimple = type.substring(type.lastIndexOf('.') + 1);
    if (typeSimple.startsWith("ArrayOf")) {
      String wrappedType = type.substring(type.indexOf("ArrayOf") + "ArrayOf".length());

      holder.addMethod(createGetter(ast, field, basename, typeSimple, wrappedType, getmeth));
      getmeth.setName(ast.newSimpleName(getmeth.getName().getIdentifier() + "AsArray"));
    }
        
  }

  @SuppressWarnings("unchecked")
  private MethodDeclaration createGetter(AST ast, FieldDeclaration field, String fieldName, String simpleFieldType,
      String wrappedType, MethodDeclaration oldGetter) {
    MethodDeclaration method = ast.newMethodDeclaration();
    Modifier publicModifier = ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD);
    method.modifiers().add(publicModifier);
    ParameterizedType returnType = ast.newParameterizedType(ast.newSimpleType(ast.newName("List")));
    returnType.typeArguments().add(ast.newSimpleType(ast.newName(wrappedType)));
    method.setReturnType2(returnType);
    method.setName(ast.newSimpleName(oldGetter.getName().getIdentifier()));

    Block body = ast.newBlock();
    IfStatement ifStatement = ast.newIfStatement();
    InfixExpression checkNull = ast.newInfixExpression();
    checkNull.setOperator(InfixExpression.Operator.EQUALS);
    checkNull.setLeftOperand(ast.newSimpleName(fieldName));
    checkNull.setRightOperand(ast.newNullLiteral());
    ifStatement.setExpression(checkNull);
    Assignment assignment = ast.newAssignment();
    assignment.setLeftHandSide(ast.newSimpleName(fieldName));
    ClassInstanceCreation create = ast.newClassInstanceCreation();
    SimpleType creationType = ast.newSimpleType(ast.newSimpleName(simpleFieldType));
    create.setType(creationType);
    assignment.setRightHandSide(create);
    ExpressionStatement assignmentStatement = ast.newExpressionStatement(assignment);
    ifStatement.setThenStatement(assignmentStatement);
    body.statements().add(ifStatement);

    ReturnStatement returnStatement = ast.newReturnStatement();
    MethodInvocation getCall = ast.newMethodInvocation();
    getCall.setExpression(ast.newSimpleName(fieldName));
    String methodName = wrappedTypeOverrides.containsKey(simpleFieldType)
        ? "get" + capitalizeFirstLetter(wrappedTypeOverrides.get(simpleFieldType))
        : "get" + wrappedType + "List";
    getCall.setName(ast.newSimpleName(methodName));
    returnStatement.setExpression(getCall);
    body.statements().add(returnStatement);
    method.setBody(body);
    return method;
  }

  @Override
  public void finish(ElementBase binding, IClassHolder holder) {
  }

  private String capitalizeFirstLetter(String s) {
    return s.substring(0, 1).toUpperCase() + s.substring(1);
  }
  
}
