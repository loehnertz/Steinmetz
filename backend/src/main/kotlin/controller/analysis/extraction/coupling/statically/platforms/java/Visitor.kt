package controller.analysis.extraction.coupling.statically.platforms.java

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.MethodReferenceExpr
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.ast.visitor.VoidVisitorAdapter


class Visitor(private val basePackageIdentifier: String) : VoidVisitorAdapter<Void>() {
    val callGraph: HashSet<Pair<String, String>> = hashSetOf()

    override fun visit(node: MethodCallExpr, aBoolean: Void?) {
        try {
            val callingClass = findDeclaringClassOfMethod(node).fullyQualifiedName.get()
            val calledClass = node.resolve().declaringType().qualifiedName

            if (callingClass.startsWith(basePackageIdentifier) && calledClass.startsWith(basePackageIdentifier)) {
                callGraph.add(Pair(calledClass, calledClass))
            }
        } catch (e: Exception) {
            // println(e.localizedMessage)
        }
        return super.visit(node, aBoolean)
    }

    override fun visit(node: MethodReferenceExpr?, aBoolean: Void?) {
        print("Method Reference: $node")
        return super.visit(node, aBoolean)
    }

    override fun visit(node: ObjectCreationExpr, aBoolean: Void?) {
        print("Method Reference: $node")
        return super.visit(node, aBoolean)
    }

    private fun findDeclaringClassOfMethod(node: Node): ClassOrInterfaceDeclaration {
        if (node is ClassOrInterfaceDeclaration) return node
        return findDeclaringClassOfMethod(node.parentNode.get())
    }
}
