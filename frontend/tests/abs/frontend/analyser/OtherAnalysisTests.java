/** 
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package abs.frontend.analyser;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;

import abs.frontend.FrontendTest;
import abs.frontend.ast.ASTNode;
import abs.frontend.ast.AwaitAsyncCall;
import abs.frontend.ast.ClassDecl;
import abs.frontend.ast.Model;
import abs.frontend.ast.ParametricDataTypeUse;
import abs.frontend.ast.Stmt;
import abs.frontend.ast.VarDeclStmt;
import abs.frontend.tests.ABSFormatter;
import abs.frontend.tests.EmptyFormatter;
import abs.frontend.typechecker.DataTypeType;
import abs.frontend.typechecker.Type;

public class OtherAnalysisTests extends FrontendTest {
    
    @Test
    public void countCOG() {
        Model m = assertParseOk("interface I { } class C { { I i = new cog C(); } Unit m() { I i = new cog C(); } } { I i; i = new cog C(); i = new C(); while (true) { i = new cog C(); }}");
        assertEquals(4, m.getNumberOfNewCogExpr());
    }
    
    @Test
    public void finalTest() {
        assertParse("interface I { } { [Final] I i; i = null; }", Config.TYPE_CHECK, Config.WITH_STD_LIB, Config.EXPECT_TYPE_ERROR);
    }

    @Test
    public void fullcopyTest() {
        Model m = assertParseOk("module M; class C {}", Config.WITH_STD_LIB);
        Model m2 = m.fullCopy();
        assertFalse(m.hasErrors());
        assertFalse(m2.hasErrors());
    }

    @Test
    public void fullcopyTest1() {
        Model m = assertParseOk("module M; class C {}", Config.WITH_STD_LIB);
        Model m2 = m.fullCopy();
        assertFalse(m.hasErrors());
        assertFalse(m2.hasErrors());
        assertTrue(m.typeCheck().isEmpty());
        assertTrue(m2.typeCheck().isEmpty());
    }

    @Test
    public void fullcopyTest2() {
        Model m = assertParseOk("module M; class C {}", Config.WITH_STD_LIB);
        assertFalse(m.hasErrors());
        assertTrue(m.typeCheck().toString(),m.typeCheck().isEmpty());
        Model m2 = m.fullCopy();
        assertFalse(m2.hasErrors());
        assertTrue(m2.typeCheck().toString(),m2.typeCheck().isEmpty());
    }
    
    @Test
    public void parsetreecopyTest() {
        Model m = assertParseOk("module M; class C {}", Config.WITH_STD_LIB);
        
        Model m2 = m.parseTreeCopy();
        assertEquals(prettyPrint(m), prettyPrint(m2));
        assertFalse(m.hasErrors());
        assertFalse(m2.hasErrors());
    }

    @Test
    public void parsetreecopyTest2() {
        Model m = assertParseOk("module M; productline TestPL;" +
        "features A, B, C; ",
        Config.WITH_STD_LIB);
        
        
        Model m2 = m.parseTreeCopy();
        assertEquals(prettyPrint(m), prettyPrint(m2));
        assertFalse(m.hasErrors());
        assertFalse(m2.hasErrors());
    }
    
    public static String prettyPrint(Model m2) {
        StringWriter writer = new StringWriter();
        PrintWriter w = new PrintWriter(writer);
        ABSFormatter f = new EmptyFormatter();
        m2.doPrettyPrint(w,f);
        return writer.toString();
    }

    //@Test
    public void awaitTest2() {
        Model.doAACrewrite = true;
        Model m = assertParseOk("data Unit; interface I { Unit m(Unit x); } class C implements I {{Unit x = await this!m(Unit());}}");
        assertFalse(m.hasErrors());
        final String p1 = prettyPrint(m);
        Model.doAACrewrite = false;
        // Model m2 = assertParseOk("data Unit; interface I { Unit m(); } class C implements I {{Unit x = await this!m();}}");
        Model m2 = assertParseOk("data Unit; interface I { Unit m(Unit x); } class C implements I {{Unit x = await this!m(Unit());}}");
        assertFalse(m2.hasErrors());
        assertEquals(p1, prettyPrint(m2));
    }
    
    //@Test
    public void awaitTest3() {
        Model.doAACrewrite = true;
        Model m = assertParseOk("data Unit; interface I { Unit m(Unit x); } class C implements I {{Unit x = await this!n(Unit());}}");
        assertFalse(m.hasErrors());
        final String p1 = prettyPrint(m);
        Model.doAACrewrite = false;
        Model m2 = assertParseOk("data Unit; interface I { Unit m(Unit x); } class C implements I {{Unit x = await this!n(Unit());}}");
        assertFalse(m2.hasErrors());
        assertEquals(p1, prettyPrint(m2));
    }

    @Test
    public void testContext1() {
        Model m = assertParseOk("data Unit; interface I { Unit m(); } class C implements I {{Unit x = await this!m();}}");
        ClassDecl cd = (ClassDecl) m.getCompilationUnit(0).getModuleDecl(0).getDecl(2);
        AwaitAsyncCall n = (AwaitAsyncCall) down(cd);
        assert n != null;
        assertThat(n.calcContextNode(VarDeclStmt.class), instanceOf(VarDeclStmt.class));
        assertThat(n.calcContextNode(Stmt.class), instanceOf(VarDeclStmt.class));
    }
    
    @Test
    public void testContext2() {
        Model m = assertParseOk("data Unit; interface I { Unit m(); } class C implements I {{Unit x = await this!m();}}");
        ClassDecl cd = (ClassDecl) m.getCompilationUnit(0).getModuleDecl(0).getDecl(2);
        AwaitAsyncCall n = (AwaitAsyncCall) down(cd);
        assert n != null;
        assertThat(n.closestParent(VarDeclStmt.class), instanceOf(VarDeclStmt.class));
        assertThat(n.closestParent(Stmt.class), instanceOf(VarDeclStmt.class));
    }
    
    @Test
    public void testContext3() {
        Model m = assertParseOk("data Fut<A>; interface I { } class C implements I {{Fut<I> f;}}");
        ClassDecl cd = (ClassDecl) m.getCompilationUnit(0).getModuleDecl(0).getDecl(2);
        VarDeclStmt n = (VarDeclStmt) cd.getInitBlock().getStmt(0);
        assert n != null;
        Type u = n.getType();
        ParametricDataTypeUse pu = (ParametricDataTypeUse) n.getVarDecl().getAccess();
        DataTypeType t = (DataTypeType) pu.getTypes().get(0);
        System.err.println(t);
    }

    
    private static ASTNode<?> down(ASTNode<?> n) {
        ASTNode<?> x = null;
        for(int i =0; i<n.getNumChild(); i++) {
            x = n.getChild(i);
            if (x == null)
                continue;
            if (x instanceof AwaitAsyncCall)
                return x;
            else {
                x = down(x);
                if (x != null)
                    return x;
            }
        }
        return null;
    }
}
