package com.szadowsz.gospel.core;

import com.szadowsz.gospel.core.data.StructTestCase;
import com.szadowsz.gospel.core.data.VarTestCase;
import com.szadowsz.gospel.core.data.numeric.DoubleTestCase;
import com.szadowsz.gospel.core.data.numeric.IntTestCase;
import com.szadowsz.gospel.core.data.util.StructIteratorTestCase;
import com.szadowsz.gospel.core.data.util.TermIteratorTestCase;
import com.szadowsz.gospel.core.lib.*;
import com.szadowsz.gospel.core.parser.ParserTestCase;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({	BuiltInTestCase.class,
				PrologTestCase.class, 
				IntTestCase.class,
				IOLibraryTestCase.class,
				DoubleTestCase.class,
				SolveInfoTestCase.class,
				StateRuleSelectionTestCase.class, 
				StructIteratorTestCase.class,
				StructTestCase.class,
				TermIteratorTestCase.class,
				TheoryTestCase.class, 
				TheoryManagerTestCase.class, 
				LibraryTestCase.class,
				JavaLibraryTestCase.class,
				ParserTestCase.class,
				SpyEventTestCase.class, 
				VarTestCase.class,
				ISOIOLibraryTestCase.class,
				SocketLibTestCase.class,
				ThreadLibraryTestCase.class
})
public class TuPrologTestSuite {}
