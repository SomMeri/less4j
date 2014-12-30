//http://stackoverflow.com/questions/7765018/returning-list-in-antlr-for-type-checking-language-java
//
// A complete lexer and grammar for CSS 2.1 as defined by the
// W3 specification.
//
// This grammar is free to use providing you retain everyhting in this header comment
// section.
//
// Author : Jim Idle, Temporal Wave LLC.
// Contact : jimi@temporal-wave.com
// Website : http://www.temporal-wave.com
// License : ANTLR Free BSD License
//
// Please visit our Web site at http://www.temporal-wave.com and try our commercial
// parsers for SQL, C#, VB.Net and more.
//
// This grammar is free to use providing you retain everything in this header comment
// section.
//
// namespaces with predicates
grammar Less;

options {
    // antlr will generate java lexer and parser
    language = Java;
    // generated parser should create abstract syntax tree
    output = AST;
    // parser will extend special super class
    superClass = SuperLessParser;
}

tokens {
  VARIABLE_DECLARATION;
  ARGUMENT_DECLARATION;
  ARGUMENT_COLLECTOR;
  EXPRESSION;
  DECLARATION;
  VARIABLE_REFERENCE;
  RULESET;
  NESTED_APPENDER;
  SELECTOR;
  EXTEND_TARGET_SELECTOR;
  SIMPLE_SELECTOR;
  ESCAPED_SELECTOR;
  EXPRESSION;
  EXPRESSION_PARENTHESES;
  ESCAPED_VALUE;
  STYLE_SHEET;
  //ANTLR seems to generate null pointers exceptions on malformed css if
  //rules match nothig and generate an empty token
  EMPTY_SEPARATOR;
  ELEMENT_NAME;
  CSS_CLASS;
  IDENT_TERM;
  NTH;
  EXTEND_IN_DECLARATION;
  PSEUDO;
  ATTRIBUTE;
  ID_SELECTOR;
  ELEMENT_SUBSEQUENT;
  CHARSET_DECLARATION;
  TERM_FUNCTION;
  TERM_FUNCTION_NAME;
  TERM;
  MEDIUM_DECLARATION;
  MEDIA_EXPRESSION;
  INTERPOLATED_MEDIA_EXPRESSION;
  IMPORT_OPTIONS;
  MEDIA_QUERY;
  MEDIUM_TYPE;
  BODY;
  MIXIN_REFERENCE;
  NAMESPACE_REFERENCE;
  DETACHED_RULESET_REFERENCE;
  DETACHED_RULESET;
  REUSABLE_STRUCTURE;
  MIXIN_PATTERN;
  GUARD_CONDITION;
  GUARD;
  DUMMY_MEANINGFULL_WHITESPACE; //ANTLR, please
  KEYFRAMES_DECLARATION;
  KEYFRAMES;
  DOCUMENT_DECLARATION;
  DOCUMENT;
  VIEWPORT;
  SUPPORTS;
  SUPPORTS_CONDITION;
  SUPPORTS_SIMPLE_CONDITION;
  SUPPORTS_QUERY;
  REUSABLE_STRUCTURE_NAME;
  PSEUDO_PAGE;
  PAGE_MARGIN_BOX;
  NAMED_EXPRESSION;
  SEMI_SPLIT_MIXIN_REFERENCE_ARGUMENTS;
  SEMI_SPLIT_MIXIN_DECLARATION_ARGUMENTS;
  INTERPOLABLE_NAME;
  UNKNOWN_AT_RULE;
  UNKNOWN_AT_RULE_NAMES_SET;
  NAMED_COMBINATOR;
}

@lexer::header {
  package com.github.sommeri.less4j.core.parser;
  import com.github.sommeri.less4j.core.parser.AntlrException;
  import com.github.sommeri.less4j.LessCompiler.Problem;
  import com.github.sommeri.less4j.LessSource;
}
 
@parser::header {
  package com.github.sommeri.less4j.core.parser;
  import com.github.sommeri.less4j.core.parser.SuperLessParser;
  import com.github.sommeri.less4j.LessCompiler.Problem;
}

//override some methods and add new members to generated lexer
@lexer::members {
    public LessLexer(LessSource source, List<Problem> errors) {
      this.errors = errors;
      this.source = source;
    }

    public LessLexer(LessSource source, CharStream input, List<Problem> errors) {
      this(source, input, new RecognizerSharedState(), errors);
    }
    
    public LessLexer(LessSource source, CharStream input, RecognizerSharedState state, List<Problem> errors) {
      super(input,state);
      this.errors = errors;
      this.source = source;
    }

  //This trick allow Lexer to emit multiple tokens per one rule.
  List tokens = new ArrayList();
  public void emit(Token token) {
        state.token = token;
        tokens.add(token);
  }
  public void emitAs(Token token, int type) {
        if (token==null)
          return ;
        token.setType(type);
        emit(token);
  }
  public Token nextToken() {
        super.nextToken();
        if ( tokens.size()==0 ) {
            return getEOFToken();
        }
        return (Token)tokens.remove(0);
  }
  //add new field
  private List<Problem> errors = new ArrayList<Problem>();
  private LessSource source;
  
  //add new method
  public List<Problem> getAllErrors() {
    return new ArrayList<Problem>(errors);
  }

  //add new method
  public boolean hasErrors() {
    return !errors.isEmpty();
  }
  
  //override method
  public void reportError(RecognitionException e) {
    errors.add(new AntlrException(source, e, getErrorMessage(e, getTokenNames())));
  }
  
}

//override some methods and add new members to generated parser
@parser::members {
  public LessParser(TokenStream input, List<Problem> errors) {
    super(input, errors);
  }
  
  public LessParser(TokenStream input, RecognizerSharedState state, List<Problem> errors) {
    super(input, state, errors);
  }
  
}

// -------------
// Main rule. This is the main entry rule for the parser, the top level
// grammar rule.
//
// A style sheet consists of an optional character set specification, an optional series
// of imports, and then the main body of style rules.
//
styleSheet
@init {enterRule(retval, RULE_STYLESHEET);}
    : ( (a+=top_level_element)*
        EOF ) -> ^(STYLE_SHEET ($a)*)
    ;
finally { leaveRule(); }
    
// -----------------
// Character set. Picks up the user specified character set, should it be present.
// Removed an empty option from this rule, ANTLR seems to have problems with this.
// https://developer.mozilla.org/en/CSS/@charset
charSet
@init {enterRule(retval, RULE_CHARSET);}
    : {predicates.isCharset(input.LT(1))}? AT_NAME STRING SEMI -> ^(CHARSET_DECLARATION AT_NAME STRING)
    ;
finally { leaveRule(); }

// ---------
// Import. Location of an external style sheet to include in the ruleset.
//
imports
@init {enterRule(retval, RULE_IMPORTS);}
    : (IMPORT_SYM | IMPORT_ONCE_SYM | IMPORT_MULTIPLE_SYM)^ (((importoptions) => importoptions)|) term (mediaQuery (COMMA mediaQuery)*)? SEMI!
    ;
finally { leaveRule(); }

importoptions:
  LPAREN a+=IDENT+ RPAREN
  -> ^(IMPORT_OPTIONS $a*)
  ;
// ---------
// Media. Introduce a set of rules that are to be used if the consumer indicates
// it belongs to the signified medium.
// Media can also have a ruleset in them.
//
//TODO media part of the grammar is throwing away tokens, so comments will not work correctly. fix that
media_queries_declaration:
    m1+=mediaQuery (n+=COMMA m+=mediaQuery)*
    -> ^(MEDIUM_DECLARATION $m1 ($n $m)*)
; 

media_top_level
@init {enterRule(retval, RULE_MEDIA);}
    : MEDIA_SYM m1+=media_queries_declaration b+=top_level_body_with_declaration
    -> ^(MEDIA_SYM $m1* $b*)
    ;
finally { leaveRule(); }

media_in_general_body
@init {enterRule(retval, RULE_MEDIA);}
    : MEDIA_SYM m1+=media_queries_declaration b+=general_body
    -> ^(MEDIA_SYM $m1* $b*)
    ;
finally { leaveRule(); }

keyframes
@init {enterRule(retval, RULE_KEYFRAME);}
    : {predicates.isKeyframes(input.LT(1))}? (AT_NAME (firstname+=keyframesname (commas+=COMMA names+=keyframesname )*)?)
      body+=general_body
    -> ^(KEYFRAMES AT_NAME ^(KEYFRAMES_DECLARATION $firstname* ($commas $names)*) $body )
    ;
finally { leaveRule(); }

keyframesname
    : IDENT | variablereference
    ;

document
@init {enterRule(retval, RULE_DOCUMENT);}
    : {predicates.isDocument(input.LT(1))}? AT_NAME 
      url_match_fn1+=term_only_function (c+=COMMA url_match_fn2+=term_only_function)*
      body+=top_level_body
    -> ^(DOCUMENT AT_NAME ^(DOCUMENT_DECLARATION $url_match_fn1 ($c $url_match_fn2)*) $body)
    ;
finally { leaveRule(); }

viewport
@init {enterRule(retval, RULE_VIEWPORT);}
    : {predicates.isViewport(input.LT(1))}? AT_NAME
      body+=general_body
    -> ^(VIEWPORT AT_NAME $body )
    ;
finally { leaveRule(); }

supports
@init {enterRule(retval, RULE_SUPPORTS);}
    : {predicates.isSupports(input.LT(1))}? AT_NAME condition+=supportsCondition
      body+=general_body
    -> ^(SUPPORTS AT_NAME $condition $body )
    ;
finally { leaveRule(); }

supportsCondition: 
     first+=simpleSupportsCondition ((IDENT)=> oper+=IDENT second+=simpleSupportsCondition)*
     -> ^(SUPPORTS_CONDITION $first ($oper $second)*)
    ;

simpleSupportsCondition:   
     (supportsQuery)=> q+=supportsQuery -> ^(SUPPORTS_SIMPLE_CONDITION $q) //( declaration ) 
     | IDENT q+=supportsCondition -> ^(SUPPORTS_SIMPLE_CONDITION IDENT $q) // not condition
     | LPAREN q+=supportsCondition RPAREN -> ^(SUPPORTS_SIMPLE_CONDITION LPAREN $q RPAREN) //nested condition
    ;
    
supportsQuery: LPAREN q+=declaration RPAREN -> ^(SUPPORTS_QUERY LPAREN $q RPAREN);

unknownAtRule
@init {enterRule(retval, RULE_UNKNOWN_AT_RULE);}
//    : {predicates.isUnknownAtRule(input.LT(1))}? AT_NAME (IDENT | variablereference)* (
    : AT_NAME names+=unknownAtRuleNamesSet? ( body+=general_body | semi+=SEMI )
    -> ^(UNKNOWN_AT_RULE AT_NAME ^(UNKNOWN_AT_RULE_NAMES_SET $names*) $body* $semi*)
    ;
finally { leaveRule(); }

unknownAtRuleNamesSet
    : (mathExprHighPrior) (COMMA (mathExprHighPrior))*
    ;
/*unknownBodylessAtRule:
@init {enterRule(retval, UNKNOWN_BODYLESS_AT_RULE);}
    : AT_NAME (IDENT | variablereference)*
    ;
finally { leaveRule(); }*/

// ---------
// Medium. The name of a medim that are particulare set of rules applies to.
//
mediaQuery
@init {enterRule(retval, RULE_MEDIUM);}
    : a+=IDENT (a+=IDENT)? (b+=IDENT c+=mediaExpression)* -> ^(MEDIA_QUERY ^(MEDIUM_TYPE $a*) ($b $c)*)
    | d=mediaExpression (e+=IDENT f+=mediaExpression)* -> ^(MEDIA_QUERY $d ($e $f)*)
    ;
finally { leaveRule(); }
    
mediaExpression
    : cssMediaExpression | interpolatedMediaExpression
    ;

cssMediaExpression
    : LPAREN a+=mediaFeature (b+=COLON c+=expr)? RPAREN -> ^(MEDIA_EXPRESSION $a* $b* $c*)
    ;

interpolatedMediaExpression
    : variablereference+ -> ^(INTERPOLATED_MEDIA_EXPRESSION variablereference+)
    ;
    
mediaFeature
    : IDENT
    ;

top_level_element
    : (mixinReferenceWithSemi)=>mixinReferenceWithSemi
    | (namespaceReferenceWithSemi)=>namespaceReferenceWithSemi
    | (reusableStructureName LPAREN)=>reusableStructure
    | (detachedRulesetReference)=>detachedRulesetReference
    | (variabledeclaration)=>variabledeclaration
    | ruleSet
    | media_top_level
    | viewport
    | keyframes
    | page
    | fontface
    | imports
    | document
    | supports
    | charSet
    | unknownAtRule
    ;

variabledeclaration
@init {enterRule(retval, RULE_VARIABLE_DECLARATION);}
    : AT_NAME COLON (a+=expr)? SEMI -> ^(VARIABLE_DECLARATION AT_NAME COLON $a* SEMI)
    ;
finally { leaveRule(); }

//used in mixinReferenceArgument
variabledeclarationNoSemi
@init {enterRule(retval, RULE_VARIABLE_DECLARATION);}
    : AT_NAME COLON (a+=expr) -> ^(VARIABLE_DECLARATION AT_NAME COLON $a* )
    ;
finally { leaveRule(); }

//This looks like the declaration, allows also three dots
reusableStructureParameterWithDefault
    : AT_NAME ((b=COLON a+=expr) | b=DOT3) -> ^(ARGUMENT_DECLARATION AT_NAME $b* $a*)
    ;

reusableStructureParameterWithoutDefault
    : atName
    | collector
    ;
    
atName:  AT_NAME  -> ^(ARGUMENT_DECLARATION AT_NAME) ;
collector:   DOT3 -> ^(ARGUMENT_DECLARATION DOT3);

variablereference
@init {enterRule(retval, RULE_VARIABLE_REFERENCE);}
    : AT_NAME | INDIRECT_VARIABLE
    ;
finally { leaveRule(); }

fontface
@init {enterRule(retval, RULE_FONT_FACE);}
    : FONT_FACE_SYM body+=general_body -> ^(FONT_FACE_SYM $body*)
    ;
finally { leaveRule(); }

page
@init {enterRule(retval, RULE_PAGE);}
    : PAGE_SYM a+=IDENT? b+=pseudoPage? c+=general_body
      -> ^(PAGE_SYM $a* $b* $c*)
    ;
finally { leaveRule(); }

pageMarginBox 
@init {enterRule(retval, RULE_PAGE_MARGIN_BOX);}
    : {predicates.isPageMarginBox(input.LT(1))}? AT_NAME general_body
    -> ^(PAGE_MARGIN_BOX AT_NAME general_body)
    ;
finally { leaveRule(); }
   

pseudoPage
@init {enterRule(retval, RULE_PSEUDO_PAGE);}
    : {predicates.directlyFollows(input.LT(-1), input.LT(1))}? COLON IDENT -> ^(PSEUDO_PAGE COLON IDENT)
      | COLON IDENT -> ^(PSEUDO_PAGE MEANINGFULL_WHITESPACE COLON IDENT)
    ;
finally { leaveRule(); }
    
topLevelOperator
    : COMMA
    | ( -> EMPTY_SEPARATOR)
    ;
    
combinator
    : GREATER
    | PLUS
    | TILDE
    | HAT
    | CAT
    | (a+=SOLIDUS a+=IDENT a+=SOLIDUS -> ^(NAMED_COMBINATOR $a*))
    | ({predicates.onEmptyCombinator(input)}?)=> -> EMPTY_COMBINATOR
    ;
    
unaryOperator
    : MINUS
    | PLUS
    ;
    
property
    : // support for star prefix browser hack - more correct and strict solution would require them to directly follow
      // normal property| merged property 
      (a+=STAR)? b+=propertyNamePart ({predicates.directlyFollows(input)}?=>b+=propertyNamePart)* ( | c+=PLUS ( | c+=UNDERSCORE))
      ->  ^(INTERPOLABLE_NAME $a* $b*) $c*
    ;
    
propertyNamePart
    :  IDENT | NUMBER | MINUS | INTERPOLATED_VARIABLE;
/*
elementName
    :  propertyNamePart ({predicates.directlyFollows(input)}?=>propertyNamePart)*;

elementName
    :  a+=elementNamePart ({predicates.directlyFollows(input.LT(-1), input.LT(1))}?=>a+=elementNamePart)* -> ^(ELEMENT_NAME $a*);
    
elementNamePart
    : STAR | IDENT | MINUS | allNumberKinds | INTERPOLATED_VARIABLE;

*/    
//we need to put comma into the tree so we can collect comments to it
//TODO: this does not accurately describes the grammar. Nested and real selectors are different.
ruleSet
@init {enterRule(retval, RULE_RULESET);}
    : ((a+=selector (
         (reusableStructureGuards LBRACE) => g+=reusableStructureGuards
         | ( a+=selectorSeparator a+=selector)*)
       ))? 
       b=general_body
     -> ^(RULESET $a* $g* $b)
    ;
finally { leaveRule(); }

selectorSeparator
    : COMMA ;

nestedAppender // this must be here because of special case & & <- the space belongs to both appenders
    :  ((MEANINGFULL_WHITESPACE? APPENDER MEANINGFULL_WHITESPACE APPENDER)=>a+=MEANINGFULL_WHITESPACE? a+=APPENDER) -> ^(NESTED_APPENDER $a* DUMMY_MEANINGFULL_WHITESPACE)
       | (
         a+=MEANINGFULL_WHITESPACE? 
         a+=APPENDER
         ( 
           (MEANINGFULL_WHITESPACE)=>a+=MEANINGFULL_WHITESPACE 
           | 
         ) 
      ) -> ^(NESTED_APPENDER $a*)
    ;

//css does not require ; in last declaration

top_level_body
    : LBRACE
            (a+=top_level_element)*
      RBRACE
     //If we remove LBRACE from the tree, a ruleset with an empty selector will report wrong line number in the warning.
     -> ^(BODY LBRACE $a* RBRACE); 

top_level_body_with_declaration
    : LBRACE
            ((declarationWithSemicolon)=> a+=declarationWithSemicolon
            | a+=top_level_element)*
      RBRACE
     //If we remove LBRACE from the tree, a ruleset with an empty selector will report wrong line number in the warning.
     -> ^(BODY LBRACE $a* RBRACE); 

general_body
    : LBRACE
            (   (declarationWithSemicolon)=> (a+=declarationWithSemicolon)
               | (ruleSet)=> a+=ruleSet
               | (mixinReferenceWithSemi)=>a+=mixinReferenceWithSemi
               | (namespaceReferenceWithSemi)=>a+=namespaceReferenceWithSemi
               | (reusableStructure)=>a+=reusableStructure
               | (detachedRulesetReference)=>a+=detachedRulesetReference
               | (variabledeclaration)=>a+=variabledeclaration
               | a+=extendInDeclarationWithSemi
               | a+=pageMarginBox 
               | a+=media_in_general_body
               | a+=viewport
               | a+=keyframes
               | a+=document
               | a+=supports
               | a+=page
               | a+=fontface
               | a+=imports
               | a+=unknownAtRule
               | SEMI
             )*
             (  rbrace+=RBRACE
                | (declaration RBRACE)=>a+=declaration rbrace+=RBRACE               
                | (mixinReference RBRACE)=>a+=mixinReference rbrace+=RBRACE
                | (namespaceReference RBRACE)=>a+=namespaceReference rbrace+=RBRACE
            //    | (detachedRulesetCallNoSemi RBRACE)=>a+=detachedRulesetCallNoSemi rbrace+=RBRACE
             )
     //If we remove LBRACE from the tree, a ruleset with an empty selector will report wrong line number in the warning.
     -> ^(BODY LBRACE $a* $rbrace*); 

// if this is changed, changes are the extendedSelector must be changed too
selector 
@init {enterRule(retval, RULE_SELECTOR);}
    : ( 
        ((combinator)=>a+=combinator | ) 
        (a+=simpleSelector | a+=nestedAppender | a+=escapedSelectorOldSyntax)
      )+
    -> ^(SELECTOR $a* ) 
    ;
finally { leaveRule(); }

// if this is changed, chances are the selector must be changed too
// Less keyword-pseudoclass "extend" takes selector as an argument. The selector can be optionally
// followed by keyword all. The grammar is ambiguous as a result and its predictability suffers.  
//extendedSelector 
//@init {enterRule(retval, RULE_SELECTOR);}
//    : ( { !predicates.matchingAllRparent(input)}?=>(
//            ((combinator)=>a+=combinator | ) 
//            (a+=simpleSelector | a+=nestedAppender | a+=escapedSelectorOldSyntax)
//        )
//      )+
//      (  { predicates.matchingAllRparent(input)}?=>b+=IDENT
//         |
//      )
//    -> ^(EXTENDED_SELECTOR ^(SELECTOR $a* ) $b*) 
//    ;
//finally { leaveRule(); }

extendTargetSelectors 
    : a+=selector (a+=selectorSeparator a+=selector)*
    -> ^(EXTEND_TARGET_SELECTOR $a*) 
    ;


//TODO clean up, this version is just a proof of concept
escapedSelectorOldSyntax: LPAREN VALUE_ESCAPE RPAREN-> ^(ESCAPED_SELECTOR VALUE_ESCAPE);

simpleSelector
    : ( (a+=elementName ( {!predicates.onEmptyCombinator(input)}?=>a+=elementSubsequent)*)
        | (a+=elementSubsequent ( {!predicates.onEmptyCombinator(input)}?=>a+=elementSubsequent)*)
      )
    -> ^(SIMPLE_SELECTOR $a*)
    ;
    
cssClassOrId    
    :   idSelector -> ^(ELEMENT_SUBSEQUENT idSelector)
        | cssClass -> ^(ELEMENT_SUBSEQUENT cssClass)
    ;
    
attribOrPseudo
    :   attrib -> ^(ELEMENT_SUBSEQUENT attrib)
        | pseudo -> ^(ELEMENT_SUBSEQUENT pseudo)
    ;

elementSubsequent
    :   cssClassOrId
      | attribOrPseudo
    ;

idSelector: 
      (b+=HASH | b+=HASH_SYMBOL b+=INTERPOLATED_VARIABLE) 
      ({predicates.directlyFollows(input.LT(-1), input.LT(1))}?=>a+=idOrClassNamePart)* 
   -> ^(ID_SELECTOR $b* $a*)
    ;
    
//A class name can be also a number e.g., .56 or .5cm or anything else that starts with a dot '.'.
cssClass
    : dot=DOT a+=idOrClassNamePart ({predicates.directlyFollows(input.LT(-1), input.LT(1))}?=>a+=idOrClassNamePart)* -> ^(CSS_CLASS $dot $a*); 
    
idOrClassNamePart
    : IDENT | MINUS | allNumberKinds | INTERPOLATED_VARIABLE;
    
elementName
    :  a+=elementNamePart ({predicates.directlyFollows(input.LT(-1), input.LT(1))}?=>a+=elementNamePart)* -> ^(ELEMENT_NAME $a*);
    
elementNamePart
    : STAR | IDENT | MINUS | allNumberKinds | INTERPOLATED_VARIABLE | UNDERSCORE;
        
allNumberKinds: NUMBER | EMS | EXS | LENGTH | ANGLE | TIME | FREQ | REPEATER | PERCENTAGE | UNKNOWN_DIMENSION;

attrib
    : (LBRACKET
    
        a+=IDENT
            (
                (
                      a+=OPEQ
                    | a+=INCLUDES
                    | a+=DASHMATCH
                    | a+=PREFIXMATCH
                    | a+=SUFFIXMATCH
                    | a+=SUBSTRINGMATCH
                )
                (
                      b+=term
                )
            )?
    
      RBRACKET)
      -> ^(ATTRIBUTE $a* $b*)
;

extendInDeclarationWithSemi
  : MEANINGFULL_WHITESPACE? APPENDER MEANINGFULL_WHITESPACE? a+=pseudo
  -> ^(EXTEND_IN_DECLARATION $a*)
  ;
  
pseudo
    : (c+=COLON c+=COLON? a=IDENT (
        (LPAREN)=>(
          { predicates.insideNth(input)}?=> LPAREN (b1=nth| b2=variablereference|b3=INTERPOLATED_VARIABLE) RPAREN
          | { predicates.insideExtend(input)}?=> LPAREN (b4+=extendTargetSelectors) RPAREN
          | { predicates.insideNot(input)}?=> LPAREN (b5=selector) RPAREN
          | LPAREN b6=pseudoparameters RPAREN 
        )
        |)
      ) -> ^(PSEUDO $c+ $a $b1* $b2* $b3* $b4* $b5* $b6*)
    ;
    
pseudoparameters:
     ((IDENT | STRING | NUMBER | variablereference) RPAREN) => term
    | selector
 ;

//TODO: add special error message here 
 nth: ((a+=PLUS | a+=MINUS)? (a+=REPEATER | a+=IDENT) ((b+=PLUS | b+=MINUS) b+=NUMBER)?
                      | (b+=PLUS | b+=MINUS)? b+=NUMBER)
      -> ^(NTH ^(TERM $a*) ^(TERM $b*));

referenceSeparator:
       GREATER
       | -> EMPTY_COMBINATOR;
 
namespaceReference
@init {enterRule(retval, RULE_NAMESPACE_REFERENCE);}
    : ((reusableStructureName referenceSeparator)=> a+=reusableStructureName referenceSeparator)+
      c+=mixinReference -> ^(NAMESPACE_REFERENCE $a* $c);
finally { leaveRule(); }

namespaceReferenceWithSemi
@init {enterRule(retval, RULE_NAMESPACE_REFERENCE);}
    : ((reusableStructureName referenceSeparator)=> a+=reusableStructureName referenceSeparator)+
      c+=mixinReference SEMI -> ^(NAMESPACE_REFERENCE $a* $c);
finally { leaveRule(); }

mixinReference
@init {enterRule(retval, RULE_MIXIN_REFERENCE);}
    : a=reusableStructureName (LPAREN b=semiSplitMixinReferenceArguments RPAREN)? c=IMPORTANT_SYM?
    -> ^(MIXIN_REFERENCE $a $b* $c*)
    ;
finally { leaveRule(); }

mixinReferenceWithSemi
@init {enterRule(retval, RULE_MIXIN_REFERENCE);}
    : a=reusableStructureName (LPAREN b=semiSplitMixinReferenceArguments RPAREN)? c=IMPORTANT_SYM? SEMI
    -> ^(MIXIN_REFERENCE $a $b* $c*)
    ; 
finally { leaveRule(); }

semiSplitMixinReferenceArguments
    : a+=mixinReferenceArguments ( a+=semicolon a+=mixinReferenceArguments)* 
    -> ^(SEMI_SPLIT_MIXIN_REFERENCE_ARGUMENTS $a*)
    ;

semicolon
    :SEMI;
    
mixinReferenceArguments
    : // nothing
    | variabledeclarationNoSemi (COMMA! variabledeclarationNoSemi)*
    | expr (COMMA! variabledeclarationNoSemi (COMMA! variabledeclarationNoSemi)*)?
    ;

reusableStructureName
    : a+=cssClassOrId -> ^(REUSABLE_STRUCTURE_NAME $a*);
    
//we can loose parentheses, because comments inside mixin definition are going to be lost anyway
reusableStructure 
@init {enterRule(retval, RULE_ABSTRACT_MIXIN_OR_NAMESPACE);}
    : a=reusableStructureName LPAREN c=semiSplitReusableStructureArguments RPAREN e=reusableStructureGuards? f=general_body
    -> ^(REUSABLE_STRUCTURE $a $c* $e* $f)
    ;
finally { leaveRule(); }

reusableStructureGuards
    : ({predicates.isWhenKeyword(input.LT(1))}?) IDENT b+=guard (COMMA d+=guard)*
    -> $b $d*
    ;
    
guard
    : a=guardCondition  (b+=IDENT c+=guardCondition)*
    -> ^(GUARD $a* ($b $c)*)
    ;    

guardCondition
    : a+=IDENT? LPAREN b+=mathExprHighPrior (b+=compareOperator b+=mathExprHighPrior)? RPAREN
    -> ^(GUARD_CONDITION $a* $b*)
    ;   

compareOperator
    : GREATER | GREATER_OR_EQUAL | OPEQ | LOWER_OR_EQUAL | LOWER
    ;     
    
semiSplitReusableStructureArguments
    : (    // nothing 
           | a+=commaSplitReusableStructureArgument ( a+=semicolon a+=commaSplitReusableStructureArgument)* (a+=semicolon)?
      ) -> ^(SEMI_SPLIT_MIXIN_DECLARATION_ARGUMENTS $a*)
    ;

commaSplitReusableStructureArgument
    : sequenceOfReusableStructureArgumentsWithDefault (COMMA! collector)?
    | patternAndNoDefaultOnlyReusableStructureArguments (COMMA! sequenceOfReusableStructureArgumentsWithDefault (COMMA! collector)?)?   
    ;
    
sequenceOfReusableStructureArgumentsWithDefault
    : reusableStructureParameterWithDefault (COMMA! reusableStructureParameterWithDefault)*
    ;
    
patternAndNoDefaultOnlyReusableStructureArguments
    : (reusableStructureParameterWithoutDefault | reusableStructurePattern) (COMMA! (reusableStructureParameterWithoutDefault | reusableStructurePattern))*
    ;    

reusableStructurePattern
    : (( a+=unaryOperator? (a+=value_term)
    ) | (
       a+=unsigned_value_term
       | a+=hexColor
    ))
    -> ^(MIXIN_PATTERN ^(TERM $a*))
    ;

//The expr is optional, because less.js supports this: "margin: ;" I do not know why, but they have it in
//their unit tests (so it was intentional)
declaration
@init {enterRule(retval, RULE_DECLARATION);}
    : property COLON expr? prio? -> ^(DECLARATION property expr? prio?)
    ;
finally { leaveRule(); }

//I had to do this to put semicolon as a last member of the declaration subtree - comments would be messed up otherwise.
declarationWithSemicolon
@init {enterRule(retval, RULE_DECLARATION);}
    : property COLON expr? prio? SEMI -> ^(DECLARATION property expr? prio?)
    ;
finally { leaveRule(); }
    
prio
    : IMPORTANT_SYM
    ;

operator
    : COMMA
    | ({predicates.onEmptySeparator(input)}?=> -> EMPTY_SEPARATOR)
    ;
    
operatorNoComma    
    : ({predicates.onEmptySeparator(input)}?=> -> EMPTY_SEPARATOR)
    ;
    
mathOperatorHighPrior
    : SOLIDUS //ratio in pure CSS
    | STAR
    ;

mathOperatorLowPrior
    : PLUS
    | MINUS
    ;

detachedRuleset
@init {enterRule(retval, RULE_DETACHED_RULESET);}
    : general_body
    -> ^(DETACHED_RULESET general_body)
    ;
finally { leaveRule(); }

detachedRulesetReference
@init {enterRule(retval, RULE_DETACHED_RULESET_REFERENCE);}
    : a+=AT_NAME (a+=LPAREN? a+=RPAREN?) SEMI
    -> ^(DETACHED_RULESET_REFERENCE $a*)
    ;
finally { leaveRule(); }

expr 
@init {enterRule(retval, RULE_EXPRESSION);}
    : (dr+=detachedRuleset | (a+=mathExprHighPrior (b+=operator c+=mathExprHighPrior)*)) -> ^(EXPRESSION $dr* $a* ($b $c)*)
    ;
finally { leaveRule(); }

mathExprHighPrior
@init {enterRule(retval, RULE_EXPRESSION);}
    : a=mathExprLowPrior (b+=mathOperatorLowPrior c+=mathExprLowPrior)* -> ^(EXPRESSION $a ($b $c)*)
    ;
finally { leaveRule(); }

mathExprLowPrior
@init {enterRule(retval, RULE_EXPRESSION);}
    : a=term (b+=mathOperatorHighPrior c+=term)* -> ^(EXPRESSION $a ($b $c)*)
    ;
finally { leaveRule(); }

term
    : (( a+=unaryOperator? (a+=value_term
    | ({predicates.onFunctionStart(input)}?=> a+=function)
    | a+=expr_in_parentheses
    | a+=variablereference
    )
    ) | (a+=unsigned_value_term
        | a+=hexColor
        | a+=escapedValue
        | a+=embeddedScript
        | a+=special_function))
    -> ^(TERM $a*)
    ;
    
term_only_function
    : (a+=function
    | a+=special_function)
    -> ^(TERM $a*)
    ;    

escapedValue: VALUE_ESCAPE -> ^(ESCAPED_VALUE VALUE_ESCAPE);

embeddedScript: EMBEDDED_SCRIPT | ESCAPED_SCRIPT;
    
expr_in_parentheses
    : LPAREN expr RPAREN -> ^(EXPRESSION_PARENTHESES LPAREN expr RPAREN );
    
value_term
    : NUMBER
    | PERCENTAGE
    | LENGTH
    | EMS
    | EXS
    | ANGLE
    | UNKNOWN_DIMENSION
    | TIME
    | FREQ
    ;

unsigned_value_term
    : STRING
    | UNICODE_RANGE
    | PERCENT
    | identifierValueTerm
    ;
    
identifierValueTerm
    : a+=IDENT ({predicates.directlyFollows(input) && predicates.notSemi(input)}?=> (a+=DOT | a+=HASH | a+=IDENT))* -> ^(IDENT_TERM $a*);
// /*({predicates.directlyFollows(input)}?=> (a+=DOT | a+=HASH | a+=IDENT))* */

special_function
    : URI | URL_PREFIX | DOMAIN 
    ;

hexColor
    : HASH -> HASH
    ;

function
    : a=functionName LPAREN b=functionParameters? RPAREN -> ^(TERM_FUNCTION $a* $b*)
    ;

//function names should allow filters: progid:DXImageTransform.Microsoft.gradient(startColorstr='#FF0000ff', endColorstr='#FFff0000', GradientType=1)
functionName
    : (a+=IDENT (a+=COLON a+=IDENT| a+=DOT a+=IDENT)*
    | a+=PERCENT)
    -> ^(TERM_FUNCTION_NAME $a*)
    ;
    
functionParameters
    : a=namedFunctionParameter (b+=COMMA c+=namedFunctionParameter)* -> ^(EXPRESSION $a ($b $c)*)
    | expr;

namedFunctionParameter
    : IDENT OPEQ term -> ^(NAMED_EXPRESSION IDENT term);
    
// ==============================================================
// LEXER
//
// The lexer follows the normative section of WWW standard as closely
// as it can. For instance, where the ANTLR lexer returns a token that
// is unambiguous for both ANTLR and lex (the standard defines tokens
// in lex notation), then the token names are equivalent.
//
// Note however that lex has a match order defined as top to bottom
// with longest match first. This results in a fairly inefficent, match,
// REJECT, match REJECT set of operations. ANTLR lexer grammars are actaully
// LL grammars (and hence LL recognizers), which means that we must
// specifically disambiguate longest matches and so on, when the lex
// like normative grammar results in ambiguities as far as ANTLR is concerned.
//
// This means that some tokens will either be combined compared to the
// normative spec, and the paresr will recognize them for what they are.
// In this case, the token will named as XXX_YYY where XXX and YYY are the
// token names used in the specification.
//
// Lex style macro names used in the spec may sometimes be used (in upper case
// version) as fragment rules in this grammar. However ANTLR fragment rules
// are not quite the same as lex macros, in that they generate actual
// methods in the recognizer class, and so may not be as effecient. In
// some cases then, the macro contents are embedded. Annotation indicate when
// this is the case.
//
// See comments in the rules for specific details.
// --------------------------------------------------------------
//
// N.B. CSS 2.1 is defined as case insensitive, but because each character
// is allowed to be written as in escaped form we basically define each
// character as a fragment and reuse it in all other rules.
// ==============================================================

fragment EMPTY_COMBINATOR: ;

// --------------------------------------------------------------
// Define all the fragments of the lexer. These rules neither recognize
// nor create tokens, but must be called from non-fragment rules, which
// do create tokens, using these fragments to either purely define the
// token number, or by calling them to match a certain portion of
// the token string.
//

fragment HEXCHAR : ('a'..'f'|'A'..'F'|'0'..'9') ;

fragment NONASCII : '\u0080'..'\uFFFF' ; // NB: Upper bound should be \u4177777

fragment UNICODE : '\\' HEXCHAR
                                (HEXCHAR
                                    (HEXCHAR
                                        (HEXCHAR
                                            (HEXCHAR HEXCHAR?)?
                                        )?
                                    )?
                                )?
                                ('\r'|'\n'|'\t'|'\f'|' ')* ;
                                
fragment ESCAPE : UNICODE | '\\' ~('\r'|'\n'|'\f'|HEXCHAR) ;

fragment NMSTART :  UNDERSCORE | DOLLAR
                        | 'a'..'z'
                        | 'A'..'Z'
                        | NONASCII
                        | ESCAPE
                        ;

fragment NMCHAR : UNDERSCORE | DOLLAR
                        | 'a'..'z'
                        | 'A'..'Z'
                        | '0'..'9'
                        | '-'
                        | NONASCII
                        | ESCAPE
                        ;
                        
fragment NAME : NMCHAR+ ;
fragment UNKNOWN_DIMENSION : NMSTART NMCHAR* ;

// The original URL did not allowed characters, '.', '=', ':', ';', ',' and so on
// I added those characters that appear in less.js css test case.
fragment URL : (
                              '['|'!'|'#'|'%'|'&'|'*'|'~'|'/'|'.'|'='|':'|';'|','|'\r'|'\n'|'\t'|' '|'+'|'?'
                            | NMCHAR | '@{'NAME'}'
                          )*
                        ;

                        
// Basic Alpha characters in upper, lower and escaped form. Note that
// whitespace and newlines are unimportant even within keywords. We do not
// however call a further fragment rule to consume these characters for
// reasons of performance - the rules are still eminently readable.
//
fragment A : ('a'|'A') ('\r'|'\n'|'\t'|'\f'|' ')*
                | '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'1'
                ;
fragment B : ('b'|'B') ('\r'|'\n'|'\t'|'\f'|' ')*
                | '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'2'
                ;
fragment C : ('c'|'C') ('\r'|'\n'|'\t'|'\f'|' ')*
                | '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'3'
                ;
fragment D : ('d'|'D') ('\r'|'\n'|'\t'|'\f'|' ')*
                | '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'4'
                ;
fragment E : ('e'|'E') ('\r'|'\n'|'\t'|'\f'|' ')*
                | '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'5'
                ;
fragment F : ('f'|'F') ('\r'|'\n'|'\t'|'\f'|' ')*
                | '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'6'
                ;
fragment G : ('g'|'G') ('\r'|'\n'|'\t'|'\f'|' ')*
                | '\\'
                        (
                              'g'
                            | 'G'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'7'
                        )
                ;
fragment H : ('h'|'H') ('\r'|'\n'|'\t'|'\f'|' ')*
                | '\\'
                        (
                              'h'
                            | 'H'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'8'
                        )
                ;
fragment I : ('i'|'I') ('\r'|'\n'|'\t'|'\f'|' ')*
                | '\\'
                        (
                              'i'
                            | 'I'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'9'
                        )
                ;
fragment J : ('j'|'J') ('\r'|'\n'|'\t'|'\f'|' ')*
                | '\\'
                        (
                              'j'
                            | 'J'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('A'|'a')
                        )
                ;
fragment K : ('k'|'K') ('\r'|'\n'|'\t'|'\f'|' ')*
                | '\\'
                        (
                              'k'
                            | 'K'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('B'|'b')
                        )
                ;
fragment L : ('l'|'L') ('\r'|'\n'|'\t'|'\f'|' ')*
                | '\\'
                        (
                              'l'
                            | 'L'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('C'|'c')
                        )
                ;
fragment M : ('m'|'M') ('\r'|'\n'|'\t'|'\f'|' ')*
                | '\\'
                        (
                              'm'
                            | 'M'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('D'|'d')
                        )
                ;
fragment N : ('n'|'N') ('\r'|'\n'|'\t'|'\f'|' ')*
                | '\\'
                        (
                              'n'
                            | 'N'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('E'|'e')
                        )
                ;
fragment O : ('o'|'O') ('\r'|'\n'|'\t'|'\f'|' ')*
                | '\\'
                        (
                              'o'
                            | 'O'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('F'|'f')
                        )
                ;
fragment P : ('p'|'P') ('\r'|'\n'|'\t'|'\f'|' ')*
                | '\\'
                        (
                              'p'
                            | 'P'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('0')
                        )
                ;
fragment Q : ('q'|'Q') ('\r'|'\n'|'\t'|'\f'|' ')*
                | '\\'
                        (
                              'q'
                            | 'Q'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('1')
                        )
                ;
fragment R : ('r'|'R') ('\r'|'\n'|'\t'|'\f'|' ')*
                | '\\'
                        (
                              'r'
                            | 'R'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('2')
                        )
                ;
fragment S : ('s'|'S') ('\r'|'\n'|'\t'|'\f'|' ')*
                | '\\'
                        (
                              's'
                            | 'S'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('3')
                        )
                ;
fragment T : ('t'|'T') ('\r'|'\n'|'\t'|'\f'|' ')*
                | '\\'
                        (
                              't'
                            | 'T'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('4')
                        )
                ;
fragment U : ('u'|'U') ('\r'|'\n'|'\t'|'\f'|' ')*
                | '\\'
                        (
                              'u'
                            | 'U'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('5')
                        )
                ;
fragment V : ('v'|'V') ('\r'|'\n'|'\t'|'\f'|' ')*
                | '\\'
                        ( 'v'
                            | 'V'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('6')
                        )
                ;
fragment W : ('w'|'W') ('\r'|'\n'|'\t'|'\f'|' ')*
                | '\\'
                        (
                              'w'
                            | 'W'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('7')
                        )
                ;
fragment X : ('x'|'X') ('\r'|'\n'|'\t'|'\f'|' ')*
                | '\\'
                        (
                              'x'
                            | 'X'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('8')
                        )
                ;
fragment Y : ('y'|'Y') ('\r'|'\n'|'\t'|'\f'|' ')*
                | '\\'
                        (
                              'y'
                            | 'Y'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('9')
                        )
                ;
fragment Z : ('z'|'Z') ('\r'|'\n'|'\t'|'\f'|' ')*
                | '\\'
                        (
                              'z'
                            | 'Z'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('A'|'a')
                        )
                ;


// -------------
// Comments. Comments may not be nested, may be multilined and are delimited
// like C comments: /* ..... */
// COMMENTS are hidden from the parser which simplifies the parser
// grammar a lot.
//
COMMENT : '/*' ( options { greedy=false; } : .*) '*/'
    
                    {
                        $channel = HIDDEN; // Comments on channel 2 in case we want to find them
                    }
                ;

//TODO: document: annoying - causes antlr null pointer exception during generation
//catch in lexer is impossible
//COMMENT_LITTLE : '//' ~(NL)* { $channel = HIDDEN; };
//COMMENT_LITTLE : '//' (( options { greedy=false; } : .* ) NL) { $channel = HIDDEN; };
COMMENT_LITTLE : '//' (~('\r\n'|'\n'))* { $channel = HIDDEN; };

//COMMENT_LITTLE : '//' (
//  (( options { greedy=false; } : .* ) NL)=>(( options { greedy=false; } : .* ) NL)
//  | (( options { greedy=false; } : .* ) EOF)=>(( options { greedy=false; } : .* ) EOF) 
//) { $channel = HIDDEN; };


// ---------------------
// HTML comment open. HTML/XML comments may be placed around style sheets so that they
// are hidden from higher scope parsing engines such as HTML parsers.
// They comment open is therfore ignored by the CSS parser and we hide
// it from the ANLTR parser.
//
CDO : '<!--'

                    {
                        $channel = 3; // CDO on channel 3 in case we want it later
                    }
                ;
    
// ---------------------
// HTML comment close. HTML/XML comments may be placed around style sheets so that they
// are hidden from higher scope parsing engines such as HTML parsers.
// They comment close is therfore ignored by the CSS parser and we hide
// it from the ANLTR parser.
//
CDC : '-->'

                    {
                        $channel = 4; // CDC on channel 4 in case we want it later
                    }
                ;
                
INCLUDES : '~=' ;
DASHMATCH : '|=' ;
PREFIXMATCH : '^=' ;
SUFFIXMATCH : '$=' ;
SUBSTRINGMATCH : '*=' ;


HAT : '^' ;
CAT : '^^' ;
TILDE : '~' ;
GREATER : '>' ;
GREATER_OR_EQUAL : '>=' ;
LOWER : '<' ;
LOWER_OR_EQUAL : '=<' ;
LBRACE : '{' ;
RBRACE : '}' ;
LBRACKET : '[' ;
RBRACKET : ']' ;
OPEQ : '=' ;
SEMI : ';' ((WS_FRAGMENT? ';')=>WS_FRAGMENT? ';')* ;
COLON : ':' ;
PERCENT: '%';
SOLIDUS : '/' ;
MINUS : '-' ;
PLUS : '+' ;
STAR : '*' ;
LPAREN : '(' ;
RPAREN : ')' ;
COMMA : ',' ;
HASH_SYMBOL: '#';
DOT : '.' ;
DOT3 : '...' ;
UNDERSCORE: '_';
DOLLAR: '$';
//TODO: this trick could be used in other places too. I may be able to avoid some predicates and token position comparisons!
fragment APPENDER_FRAGMENT: '&';
fragment MEANINGFULL_WHITESPACE: ;
//TODO explain why I need appender fragment
APPENDER: ws1=WS_FRAGMENT? app=APPENDER_FRAGMENT ws2=WS_FRAGMENT? {
  emitAs($ws1, MEANINGFULL_WHITESPACE);
  emitAs($app, APPENDER);
  emitAs($ws2, MEANINGFULL_WHITESPACE);
};

// -----------------
// Literal strings. Delimited by either ' or "
// original grammar considered a single isolated letter to be a string too.
// therefore h1, h2 > a > p, h3 would be tokenized as IDENT COLON IDENT GREATER STRING GREATER STRING ...
// removed that option
//the original string definition did not supported escaping '\\\\'*
fragment INVALID :;
//This would normally contains all allowed escape symbols. As we are doing compiler into css, we do not
//care about the exact meaning of escaped symbol, nor we do care whether that make sense.
//we just want to eat a character that follows escape symbol.
fragment ESCAPED_SYMBOL : '\\' . ;
STRING : '\'' ( ESCAPED_SYMBOL | ~('\n'|'\r'|'\f'|'\\'|'\'') )*
                    (
                          '\''
                        | { $type = INVALID; }
                    )
                    
                | '"' ( ESCAPED_SYMBOL | ~('\n'|'\r'|'\f'|'\\'|'"') )*
                    (
                          '"'
                        | { $type = INVALID; }
                    )
                ;
                
VALUE_ESCAPE : '~' '\'' ( ESCAPED_SYMBOL | ~('\n'|'\r'|'\f'|'\\'|'\'') )*
                    (
                          '\''
                        | { $type = INVALID; }
                    )
                    
                | '~' '"' ( ESCAPED_SYMBOL | ~('\n'|'\r'|'\f'|'\\'|'"') )*
                    (
                          '"'
                        | { $type = INVALID; }
                    )
                ;   

EMBEDDED_SCRIPT : '`' ( ESCAPED_SYMBOL | ~('\f'|'\\'|'`') )*
                    (
                          '`'
                        | { $type = INVALID; }
                    );   

ESCAPED_SCRIPT : '~' '`' ( ESCAPED_SYMBOL | ~('\f'|'\\'|'`') )*
                    (
                          '`'
                        | { $type = INVALID; }
                    );   
                    
                
// -------------
// Identifier. Identifier tokens pick up properties names and values
//
//
IDENT : '-'? NMSTART NMCHAR*;
fragment UNICODE_RANGE_HEX: HEXCHAR | '?';
UNICODE_RANGE: U PLUS UNICODE_RANGE_HEX+ (MINUS UNICODE_RANGE_HEX+)?;

// -------------
// Reference. Reference to an element in the body we are styling, such as <XXXX id="reference">
//
fragment HASH_FRAGMENT : '#' NAME ;
HASH : HASH_FRAGMENT ;

IMPORT_SYM : '@' I M P O R T;
IMPORT_ONCE_SYM : '@' I M P O R T MINUS O N C E;
IMPORT_MULTIPLE_SYM : '@' I M P O R T  MINUS M U L T I P L E;
PAGE_SYM : '@' P A G E ;
MEDIA_SYM : '@' M E D I A ;
FONT_FACE_SYM : '@' F O N T MINUS F A C E ;

AT_NAME : '@' NAME ;
INDIRECT_VARIABLE : '@' '@' NAME ;
INTERPOLATED_VARIABLE : '@' LBRACE NAME RBRACE;

IMPORTANT_SYM : '!' (WS|COMMENT)* I M P O R T A N T ;

// ---------
// Numbers. Numbers can be followed by pre-known units or unknown units
// as well as '%' it is a precentage. Whitespace cannot be between
// the numebr and teh unit or percent. Hence we scan any numeric, then
// if we detect one of the lexical sequences for unit tokens, we change
// the lexical type dynamically.
//
// Here we first define the various tokens, then we implement the
// number parsing rule.
//
fragment EMS :; // 'em'
fragment EXS :; // 'ex'
fragment LENGTH :; // 'px'. 'cm', 'mm', 'in'. 'pt', 'pc'
fragment ANGLE :; // 'deg', 'rad', 'grad'
fragment TIME :; // 'ms', 's'
fragment FREQ :; // 'khz', 'hz'
fragment REPEATER :; // n found in n-th child formulas if I would not do that, the dimension would eat it. 
fragment PERCENTAGE :; // '%'

// there is no reason to require a dot inside a number
fragment PURE_NUMBER: '0'..'9'+ ('.' '0'..'9'+)?
            | '.' '0'..'9'+;
            
NUMBER
    : (PURE_NUMBER
              
        )
        (
              (E (M|X))=>
                E
                (
                      M { $type = EMS; }
                    | X { $type = EXS; }
                )
            | (P(X|T|C))=>
                P
                (
                      X
                    | T
                    | C
                )
                            { $type = LENGTH; }
            | (C M)=>
                C M { $type = LENGTH; }
            | (M (M|S))=>
                M
                (
                      M { $type = LENGTH; }
            
                    | S { $type = TIME; }
                )
            | (I N)=>
                I N { $type = LENGTH; }
            
            | (D E G)=>
                D E G { $type = ANGLE; }
            | (R A D)=>
                R A D { $type = ANGLE; }
            
            | (S)=>S { $type = TIME; }
                
            | (K? H Z)=>
                K? H Z { $type = FREQ; }

            | (N ~('a'..'z'|'A'..'Z')|'0'..'9')=>
                N { $type = REPEATER; }
            
            | PERCENT { $type = PERCENTAGE; }
            | UNKNOWN_DIMENSION { $type = UNKNOWN_DIMENSION; }
            | // Just a number
        )
    ;
    
// ------------
// special functions that takes url/uri as a parameter, but do not enclose it in quotes.
URI : ((U R L '(' ((WS)=>WS)? URL WS? ')') 
           => (U R L '(' ((WS)=>WS)? URL WS? ')'))
      | U R L { $type=IDENT; } //I have to do this, because lexer never hoists syntactic predicates
      | U R { $type=IDENT; } //I have to do this, because lexer never hoists syntactic predicates
      | U { $type=IDENT; } //I have to do this, because lexer never hoists syntactic predicates
    ;

URL_PREFIX : ((U R L MINUS P R E F I X '(' ((WS)=>WS)? URL WS? ')') 
           => (U R L MINUS P R E F I X '(' ((WS)=>WS)? URL WS? ')'))
      | U (R (L (MINUS (P (R (E (F (I (X)?)?)?)?)?)?)?)?)? { $type=IDENT; } //I have to do this, because lexer never hoists syntactic predicates
    ;

DOMAIN : ((D O M A I N '(' ((WS)=>WS)? URL WS? ')') 
           => (D O M A I N '(' ((WS)=>WS)? URL WS? ')'))
      | D (O (M (A (I (N)?)?)?)?)? { $type=IDENT; } //I have to do this, because lexer never hoists syntactic predicates
    ;
    
// -------------
// Whitespace. Though the W3 standard shows a Yacc/Lex style parser and lexer
// that process the whitespace within the parser, ANTLR does not
// need to deal with the whitespace directly in the parser.
//
fragment WS_FRAGMENT : (' '|'\t')+ ;
WS : WS_FRAGMENT { $channel = HIDDEN; } ;
fragment NL : ('\r' '\n'? | '\n');
NEW_LINE: NL { $channel = HIDDEN; } ;

// -------------
// Illegal. Any other character shoudl not be allowed.
//


