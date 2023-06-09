/*
 * Copyright 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.javascript.jscomp;

import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.NodeTraversal.Callback;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

/**
 * Checks for certain uses of the {@code this} keyword that are considered
 * unsafe because they are likely to reference the global {@code this} object
 * unintentionally.
 *
 * <p>A use of {@code this} is considered unsafe if it's on the left side of an
 * assignment or a property access, and not inside one of the following:
 * <ol>
 * <li>a prototype method
 * <li>a function annotated with {@code @constructor}
 * <li>a function annotated with {@code @this}.
 * <li>a function where there's no logical place to put a
 *     {@code this} annotation.
 * </ol>
 *
 * <p>Note that this check does not track assignments of {@code this} to
 * variables or objects. The code
 * <pre>
 * function evil() {
 *   var a = this;
 *   a.useful = undefined;
 * }
 * </pre>
 * will not get flagged, even though it is semantically equivalent to
 * <pre>
 * function evil() {
 *   this.useful = undefined;
 * }
 * </pre>
 * which would get flagged.
 *
*
*
 */
final class CheckGlobalThis implements Callback {

  static final DiagnosticType GLOBAL_THIS = DiagnosticType.warning(
      "JSC_USED_GLOBAL_THIS",
      "dangerous use of the global 'this' object");

  private final AbstractCompiler compiler;
  private final CheckLevel level;

  /**
   * If {@code assignLhsChild != null}, then the node being traversed is
   * a descendant of the first child of an ASSIGN node. assignLhsChild's
   * parent is this ASSIGN node.
   */
  private Node assignLhsChild = null;

  CheckGlobalThis(AbstractCompiler compiler, CheckLevel level) {
    this.compiler = compiler;
    this.level = level;
  }

  /**
   * Since this pass reports errors only when a global {@code this} keyword
   * is encountered, there is no reason to traverse non global contexts.
   */
  public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {

    if (n.getType() == Token.FUNCTION) {
      // Don't traverse functions that are constructors or have the @this
      // or @override annotation.
      JSDocInfo jsDoc = getFunctionJsDocInfo(n);
      if (jsDoc != null &&
          (jsDoc.isConstructor() ||
           jsDoc.isInterface() ||
           jsDoc.hasThisType() ||
           jsDoc.isOverride())) {
        return false;
      }

      // Don't traverse functions unless they would normally
      // be able to have a @this annotation associated with them. e.g.,
      // var a = function() { }; // or
      // function a() {} // or
      // a.x = function() {}; // or
      // var a = {x: function() {}};
      int pType = parent.getType();
      if (!(pType == Token.BLOCK ||
            pType == Token.SCRIPT ||
            pType == Token.NAME ||
            pType == Token.ASSIGN ||
            pType == Token.STRING ||
            pType == Token.NUMBER)) {
        return false;
      }

      // Don't traverse functions that are getting lent to a prototype.
      Node gramps = parent.getParent();
      if (NodeUtil.isObjectLitKey(parent, gramps)) {
        if (gramps.getType() == Token.GETPROP &&
                gramps.getLastChild().getString().equals("prototype")) {
          return false;
        }
      }
    }

    if (parent != null && parent.getType() == Token.ASSIGN) {
      Node lhs = parent.getFirstChild();
      Node rhs = lhs.getNext();

      if (n == lhs) {
        // Always traverse the left side of the assignment. To handle
        // nested assignments properly (e.g., (a = this).property = c;),
        // assignLhsChild should not be overridden.
        if (assignLhsChild == null) {
          assignLhsChild = lhs;
        }
      } else {
        // Only traverse the right side if it's not an assignment to a prototype
        // property or subproperty.
        if (NodeUtil.isGet(lhs)) {
          if (lhs.getType() == Token.GETPROP &&
              lhs.getLastChild().getString().equals("prototype")) {
            return false;
          }
          Node llhs = lhs.getFirstChild();
          if (llhs.getType() == Token.GETPROP &&
              llhs.getLastChild().getString().equals("prototype")) {
            return false;
          }
        }
      }
    }

    return true;
  }

  public void visit(NodeTraversal t, Node n, Node parent) {
    if (n.getType() == Token.THIS && shouldReportThis(n, parent)) {
      compiler.report(t.makeError(n, level, GLOBAL_THIS));
    }
    if (n == assignLhsChild) {
      assignLhsChild = null;
    }
  }

  private boolean shouldReportThis(Node n, Node parent) {
    if (assignLhsChild != null) {
      // Always report a THIS on the left side of an assign.
      return true;
    }

    // Also report a THIS with a property access.
    return parent != null && NodeUtil.isGet(parent);
  }

  /**
   * Gets a function's JSDoc information, if it has any. Checks for a few
   * patterns (ellipses show where JSDoc would be):
   * <pre>
   * ... function() {}
   * ... x = function() {};
   * var ... x = function() {};
   * ... var x = function() {};
   * </pre>
   */
  private JSDocInfo getFunctionJsDocInfo(Node n) {
    JSDocInfo jsDoc = n.getJSDocInfo();
    Node parent = n.getParent();
    if (jsDoc == null) {
      int parentType = parent.getType();
      if (parentType == Token.NAME || parentType == Token.ASSIGN) {
        jsDoc = parent.getJSDocInfo();
        if (jsDoc == null && parentType == Token.NAME) {
          Node gramps = parent.getParent();
          if (gramps.getType() == Token.VAR) {
            jsDoc = gramps.getJSDocInfo();
          }
        }
      }
    }
    return jsDoc;
  }
}
