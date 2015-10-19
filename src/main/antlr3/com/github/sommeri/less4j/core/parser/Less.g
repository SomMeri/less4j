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

//FIXME !!!! (GLOBAL) selectors use a lot of syntactic predicates in loops that should not be necessary anymore - were only temporary 
//FIXME !!!! (GLOBAL) pri handling is fragile

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
  import com.github.sommeri.less4j.core.parser.LexerLogic;
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

  protected LexerLogic lexerLogic = new LexerLogic();

  //This trick allow Lexer to emit multiple tokens per one rule.
  List tokens = new ArrayList();
  public void emit(Token token) {
        state.token = token;
        tokens.add(token);
  }
  public void emitAs(Token token, int type) {
        emitAs(token, type, DEFAULT_TOKEN_CHANNEL);
  }
  public void emitAs(Token token, int type, int channel) {
        if (token==null)
          return ;
        token.setType(type);
        token.setChannel(channel);
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

ident_nth: IDENT_NTH_CHILD | NTH_LAST_CHILD | NTH_OF_TYPE | NTH_LAST_OF_TYPE;
ident_keywords: IDENT_WHEN;

ident_special_pseudoclasses: ident_nth | IDENT_EXTEND;

ident: IDENT | IDENT_NOT | ident_keywords | ident_special_pseudoclasses;
ident_except_when: IDENT | IDENT_NOT | ident_special_pseudoclasses;
ident_except_not: IDENT | ident_keywords | ident_special_pseudoclasses;
ident_general_pseudo: IDENT | IDENT_NOT | ident_keywords;
// -------------
// Main rule. This is the main entry rule for the parser, the top level
// grammar rule.
//
// A style sheet consists of an optional character set specification, an optional series
// of imports, and then the main body of style rules.
//
//FIXME !!!! (NOW)
styleSheet
@init {enterRule(retval, RULE_STYLESHEET);}
    : ( ws (a+=top_level_element ws)*
        EOF ) -> ^(STYLE_SHEET ($a)*)
    ;
finally { leaveRule(); }

expression_full_done: ws a+=expression_full ws EOF -> $a*;
    
//styleSheet
//@init {enterRule(retval, RULE_STYLESHEET);}
//    : ( ws a+=reusableStructureName ws
//        EOF ) -> ^(STYLE_SHEET ($a)*)
//    ;
//finally { leaveRule(); }
// -----------------
// Character set. Picks up the user specified character set, should it be present.
// Removed an empty option from this rule, ANTLR seems to have problems with this.
// https://developer.mozilla.org/en/CSS/@charset
charSet
@init {enterRule(retval, RULE_CHARSET);}
    : AT_CHARSET ws STRING ws SEMI -> ^(CHARSET_DECLARATION AT_CHARSET STRING)
    ;
finally { leaveRule(); }

// ---------
// Import. Location of an external style sheet to include in the ruleset.
//
imports
@init {enterRule(retval, RULE_IMPORTS);}
      : (IMPORT_SYM | IMPORT_ONCE_SYM | IMPORT_MULTIPLE_SYM)^ ws! (((importoptions) => importoptions ws!)|) term (mandatory_ws! mediaQuery ws! (COMMA ws! mediaQuery ws!)*)? SEMI!
    ;
finally { leaveRule(); }

importoptions:
  LPAREN (ws a+=ident ws COMMA)* (ws a+=ident)? RPAREN
  -> ^(IMPORT_OPTIONS $a*)
  ;
// ---------
// Media. Introduce a set of rules that are to be used if the consumer indicates
// it belongs to the signified medium.
// Media can also have a ruleset in them.
//
//TODO media part of the grammar is throwing away tokens, so comments will not work correctly. fix that
media_queries_declaration:
    m1+=mediaQuery (ws n+=COMMA ws m+=mediaQuery)*
    -> ^(MEDIUM_DECLARATION $m1 ($n $m)*)
; 

media_top_level
@init {enterRule(retval, RULE_MEDIA);}
    : MEDIA_SYM mandatory_ws m1+=media_queries_declaration ws b+=top_level_body_with_declaration
    -> ^(MEDIA_SYM $m1* $b*)
    ;
finally { leaveRule(); }

media_in_general_body
@init {enterRule(retval, RULE_MEDIA);}
    : MEDIA_SYM mandatory_ws m1+=media_queries_declaration ws b+=general_body
    -> ^(MEDIA_SYM $m1* $b*)
    ;
finally { leaveRule(); }

keyframes
@init {enterRule(retval, RULE_KEYFRAME);}
    : (AT_KEYFRAMES (ws firstname+=keyframesname ( ws commas+=COMMA ws names+=keyframesname )*)?)
      ws body+=general_body
    -> ^(KEYFRAMES AT_KEYFRAMES ^(KEYFRAMES_DECLARATION $firstname* ($commas $names)*) $body )
    ;
finally { leaveRule(); }

keyframesname
    : ident | variablereference | STRING
    ;

document
@init {enterRule(retval, RULE_DOCUMENT);}
    : AT_DOCUMENT ws
      url_match_fn1+=term_only_function (ws c+=COMMA ws url_match_fn2+=term_only_function)*
      ws body+=top_level_body
    -> ^(DOCUMENT AT_DOCUMENT ^(DOCUMENT_DECLARATION $url_match_fn1 ($c $url_match_fn2)*) $body)
    ;
finally { leaveRule(); }

viewport
@init {enterRule(retval, RULE_VIEWPORT);}
    : AT_VIEWPORT ws
      body+=general_body
    -> ^(VIEWPORT AT_VIEWPORT $body )
    ;
finally { leaveRule(); }

supports
@init {enterRule(retval, RULE_SUPPORTS);}
    : AT_SUPPORTS ws condition+=supportsCondition ws
      body+=general_body
    -> ^(SUPPORTS AT_SUPPORTS $condition $body )
    ;
finally { leaveRule(); }

supportsCondition: 
     first+=simpleSupportsCondition ((ws ident_except_not)=> ws oper+=ident_except_not ws second+=simpleSupportsCondition)*
     -> ^(SUPPORTS_CONDITION $first ($oper $second)*)
    ;

simpleSupportsCondition:   
     (supportsQuery)=> q+=supportsQuery -> ^(SUPPORTS_SIMPLE_CONDITION $q) //( declaration ) 
     | IDENT_NOT ws q+=supportsCondition -> ^(SUPPORTS_SIMPLE_CONDITION IDENT_NOT $q) // not condition
     | LPAREN ws q+=supportsCondition ws RPAREN -> ^(SUPPORTS_SIMPLE_CONDITION LPAREN $q RPAREN) //nested condition
    ;
    
supportsQuery: LPAREN ws q+=declaration ws RPAREN -> ^(SUPPORTS_QUERY LPAREN $q RPAREN);

unknownAtRule
@init {enterRule(retval, RULE_UNKNOWN_AT_RULE);}
    : AT_NAME (ws names+=unknownAtRuleNamesSet)? ws ( body+=general_body | semi+=SEMI )
    -> ^(UNKNOWN_AT_RULE AT_NAME ^(UNKNOWN_AT_RULE_NAMES_SET $names*) $body* $semi*)
    ;
finally { leaveRule(); }

unknownAtRuleNamesSet
    : (mathExprHighPrior) (ws! COMMA (ws! mathExprHighPrior))*
    ;

// ---------
// Medium. The name of a medim that are particulare set of rules applies to.
//
mediaQuery
@init {enterRule(retval, RULE_MEDIUM);}
    : a+=ident (ws a+=ident)? (ws b+=ident ws c+=mediaExpression)* -> ^(MEDIA_QUERY ^(MEDIUM_TYPE $a*) ($b $c)*)
    | d=mediaExpression (ws e+=ident ws f+=mediaExpression)* -> ^(MEDIA_QUERY $d ($e $f)*)
    ;
finally { leaveRule(); }
    
mediaExpression
    : cssMediaExpression | interpolatedMediaExpression
    ;

cssMediaExpression
    : LPAREN ws a+=mediaFeature ws (b+=COLON ws c+=expression_full ws)? RPAREN -> ^(MEDIA_EXPRESSION $a* $b* $c*)
    ;

interpolatedMediaExpression
    : variablereference (ws variablereference)* -> ^(INTERPOLATED_MEDIA_EXPRESSION variablereference+)
    ;
    
mediaFeature
    : ident
    ;

top_level_element
    : 
    (mixinReferenceWithSemi)=>mixinReferenceWithSemi
    | (namespaceReferenceWithSemi)=>namespaceReferenceWithSemi
    | (reusableStructureName ws LPAREN)=>reusableStructure
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
    
variablename:
  AT_NAME | IMPORT_SYM | IMPORT_ONCE_SYM | IMPORT_MULTIPLE_SYM | PAGE_SYM | MEDIA_SYM | FONT_FACE_SYM | AT_KEYFRAMES | AT_DOCUMENT | AT_VIEWPORT | AT_SUPPORTS | AT_CHARSET
  ;

variabledeclaration
@init {enterRule(retval, RULE_VARIABLE_DECLARATION);}
    : variablename ws COLON (ws a+=expression_full)? ws SEMI -> ^(VARIABLE_DECLARATION variablename COLON $a* SEMI)
    ;
finally { leaveRule(); }

//used in mixinReferenceArgument
variabledeclarationNoSemi
@init {enterRule(retval, RULE_VARIABLE_DECLARATION);}
    : variablename ws COLON ws a+=expression_full -> ^(VARIABLE_DECLARATION variablename COLON $a* )
    ;
finally { leaveRule(); }

//This looks like the declaration, allows also three dots
reusableStructureParameterWithDefault
    : AT_NAME ((ws b=COLON ws a+=expression_full) | ws b=DOT3) -> ^(ARGUMENT_DECLARATION AT_NAME $b* $a*)
    ;

reusableStructureParameterWithoutDefault
    : atName
    | collector
    ;
    
atName:  AT_NAME  -> ^(ARGUMENT_DECLARATION AT_NAME) ;
collector:   DOT3 -> ^(ARGUMENT_DECLARATION DOT3);

variablereference
@init {enterRule(retval, RULE_VARIABLE_REFERENCE);}
    : a=variablename b+=DOT3? -> ^(VARIABLE_REFERENCE $a $b*)  
    | INDIRECT_VARIABLE DOT3?
    ;
finally { leaveRule(); }

fontface
@init {enterRule(retval, RULE_FONT_FACE);}
    : FONT_FACE_SYM ws body+=general_body -> ^(FONT_FACE_SYM $body*)
    ;
finally { leaveRule(); }

page
@init {enterRule(retval, RULE_PAGE);}
    : PAGE_SYM (ws a+=ident)? (b+=pseudoPage)? ws c+=general_body
      -> ^(PAGE_SYM $a* $b* $c*)
    ;
finally { leaveRule(); }

pageMarginBox 
@init {enterRule(retval, RULE_PAGE_MARGIN_BOX);}
    : {lexerLogic.isPageMarginBox(input.LT(1))}? AT_NAME ws general_body
    -> ^(PAGE_MARGIN_BOX AT_NAME general_body)
    ;
finally { leaveRule(); }
   
pseudoPage
@init {enterRule(retval, RULE_PSEUDO_PAGE);}
    : COLON ws ident -> ^(PSEUDO_PAGE COLON ident)
      | mandatory_ws COLON ws ident -> ^(PSEUDO_PAGE MEANINGFULL_WHITESPACE COLON ident)
    ;
finally { leaveRule(); }
    
topLevelOperator
    : COMMA
    | ( -> EMPTY_SEPARATOR)
    ;

combinator
    : (ws GREATER ws)=>(ws! GREATER ws!) 
    | (ws PLUS ws )=>(ws! PLUS ws!)
    | (ws TILDE ws )=>(ws! TILDE ws!)
    | (ws HAT ws )=>(ws! HAT ws!)
    | (ws CAT ws )=>(ws! CAT ws!)
    | (ws a+=SOLIDUS b+=ident c+=SOLIDUS ws)=>(ws a+=SOLIDUS b+=ident c+=SOLIDUS ws -> ^(NAMED_COMBINATOR $a* $b* $c*))
    | (mandatory_ws)=> mandatory_ws -> EMPTY_COMBINATOR
    ;
    
property
    : // support for star prefix browser hack - more correct and strict solution would require them to directly follow
      // normal property| merged property 
      (a+=STAR ws)? b+=propertyNamePart (b+=propertyNamePart)* ( | ws c+=PLUS ( | ws c+=UNDERSCORE))
      ->  ^(INTERPOLABLE_NAME $a* $b*) $c*
    ;
    
propertyNamePart
    :  ident | NUMBER | MINUS | INTERPOLATED_VARIABLE;

//we need to put comma into the tree so we can collect comments to it
//TODO: this does not accurately describes the grammar. Nested and real selectors are different.
// there is no whitespace before selector, cause leading selector combinator should eat it
//FIXME: !!! test ending comma
ruleSet
@init {enterRule(retval, RULE_RULESET);}
    : 
      ((((selector)=>a+=selector | )(
           (ws reusableStructureGuards ws LBRACE)=> ws g+=reusableStructureGuards
          |((ws a+=selectorSeparator a+=selector)=>(ws a+=selectorSeparator a+=selector))* (ws selectorSeparator)?
         )
         ) ws)
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
            (ws a+=top_level_element)*
      ws RBRACE
     //If we remove LBRACE from the tree, a ruleset with an empty selector will report wrong line number in the warning.
     -> ^(BODY LBRACE $a* RBRACE); 

top_level_body_with_declaration
    : LBRACE ( ws | (
                (ws declarationWithSemicolon)=>(ws a+=declarationWithSemicolon)
                | (ws a+=top_level_element)=>(ws a+=top_level_element)
            )+) ws
      RBRACE
     //If we remove LBRACE from the tree, a ruleset with an empty selector will report wrong line number in the warning.
     -> ^(BODY LBRACE $a* RBRACE); 

general_body
  : LBRACE ( 
                 (ws declarationWithSemicolon)=> (ws a+=declarationWithSemicolon)
               | (ws ruleSet)=>(ws a+=ruleSet)
               | (ws mixinReferenceWithSemi)=>(ws a+=mixinReferenceWithSemi)
               | (ws namespaceReferenceWithSemi)=>(ws a+=namespaceReferenceWithSemi)
               | (ws reusableStructure)=>(ws a+=reusableStructure)
               | (ws detachedRulesetReference)=>(ws a+=detachedRulesetReference)
               | (ws variabledeclaration)=>(ws a+=variabledeclaration)
               | (ws a+=extendInDeclarationWithSemi)=>(ws a+=extendInDeclarationWithSemi)
               | (ws a+=pageMarginBox)=>(ws a+=pageMarginBox ) 
               | (ws media_in_general_body)=>(ws a+=media_in_general_body)
               | (ws viewport)=>(ws a+=viewport)
               | (ws keyframes)=>(ws a+=keyframes)
               | (ws document)=>(ws a+=document)
               | (ws supports)=>(ws a+=supports)
               | (ws page)=>(ws a+=page)
               | (ws fontface)=>(ws a+=fontface)
               | (ws imports)=>(ws a+=imports)
               | (ws unknownAtRule)=>(ws a+=unknownAtRule)
               | (ws SEMI)=>(ws SEMI)
             )*
            (   (ws declaration ws RBRACE)=>(ws a+=declaration ws rbrace+=RBRACE)  
              | (ws mixinReference ws RBRACE)=>(ws a+=mixinReference ws rbrace+=RBRACE)
              | (ws namespaceReference ws RBRACE)=>(ws a+=namespaceReference ws rbrace+=RBRACE)
              | (ws rbrace+=RBRACE)
//            | (detachedRulesetCallNoSemi ws RBRACE)=>(a+=detachedRulesetCallNoSemi ws rbrace+=RBRACE)
           )
  -> ^(BODY LBRACE $a* $rbrace*)
  ;
  
ws_semi: ws SEMI;
  
selector 
@init {enterRule(retval, RULE_SELECTOR);}
    : ((combinator)=>(a+=combinator) | ) (a+=selector_simple_and_appenders) 
      ((combinator selector_simple_and_appenders)=>(a+=combinator a+=selector_simple_and_appenders))*
    -> ^(SELECTOR $a* ) 
    ;
finally { leaveRule(); }

selector_simple_and_appenders
@init {enterRule(retval, "selector_simple_and_appenders");} // REMOVE
   : (a+=elementName | a+=elementSubsequent | a+=nestedAppender | escapedSelectorOldSyntax)+
   ;
finally { leaveRule(); }

//TODO clean up, this version is just a proof of concept
escapedSelectorOldSyntax: LPAREN VALUE_ESCAPE RPAREN-> ^(ESCAPED_SELECTOR VALUE_ESCAPE);

// if this is changed, chances are the selector must be changed too
// Less keyword-pseudoclass "extend" takes selector as an argument. The selector can be optionally
// followed by keyword all. The grammar is ambiguous as a result and its predictability suffers.  
extendTargetSelectors 
    : a+=selector (ws a+=selectorSeparator ws a+=selector)*
    -> ^(EXTEND_TARGET_SELECTOR $a*) 
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
    : cssClassOrId | 
      attribOrPseudo
    ;

idSelector: 
      (b+=HASH | b+=HASH_SYMBOL b+=INTERPOLATED_VARIABLE) ((idOrClassNamePart)=> a+=idOrClassNamePart)* 
   -> ^(ID_SELECTOR $b* $a*)
    ;
    
//A class name can be also a number e.g., .56 or .5cm or anything else that starts with a dot '.'.
cssClass
    : dot=DOT ((idOrClassNamePart)=>a+=idOrClassNamePart)+ -> ^(CSS_CLASS $dot $a*); 
    
idOrClassNamePart
    : ident_except_when | MINUS | allNumberKinds | INTERPOLATED_VARIABLE;
    
elementName
    :  ((elementNamePart)=> a+=elementNamePart)+ -> ^(ELEMENT_NAME $a*);
    
elementNamePart
    : STAR | IDENT | MINUS | allNumberKinds | INTERPOLATED_VARIABLE | UNDERSCORE;
        
allNumberKinds: NUMBER | EMS | EXS | LENGTH | ANGLE | TIME | FREQ | REPEATER | PERCENTAGE | UNKNOWN_DIMENSION;

attrib
    : (LBRACKET ws a+=ident
            ( ws
                (
                      b+=OPEQ
                    | b+=INCLUDES
                    | b+=DASHMATCH
                    | b+=PREFIXMATCH
                    | b+=SUFFIXMATCH
                    | b+=SUBSTRINGMATCH
                )
                (
                      c+=term
                )
            )?
    
      ws RBRACKET) -> ^(ATTRIBUTE $a* $b* $c*)
;

extendInDeclarationWithSemi
  : MEANINGFULL_WHITESPACE? APPENDER MEANINGFULL_WHITESPACE? a+=pseudo
  -> ^(EXTEND_IN_DECLARATION $a*)
  ;
  
pseudo
    : (c+=COLON (c+=COLON)? ( 
          ((ident_nth)=> ar+=ident_nth LPAREN ws (b1=nth| b2=variablereference|b3=INTERPOLATED_VARIABLE) ws RPAREN)
        | (IDENT_EXTEND)=>(at+=IDENT_EXTEND LPAREN ws (b4+=extendTargetSelectors) ws RPAREN)
        | (IDENT_NOT)=>(at+=IDENT_NOT LPAREN ws (b5=selector) ws RPAREN)
        | (ident_general_pseudo LPAREN)=>(ar+=ident_general_pseudo LPAREN ws b6=pseudoparameters ws RPAREN)
        |  ar+=ident
    )) -> ^(PSEUDO $c+ $ar* $at* $b1* $b2* $b3* $b4* $b5* $b6*)
    ;
    
pseudoparameters:
     ((ident | STRING | NUMBER | variablereference) ws RPAREN) => term
    | selector
 ;

//TODO: add special error message here 
 nth: (((a+=PLUS | a+=MINUS) ws )? (a+=REPEATER | b+=ident) (ws (c+=PLUS | c+=MINUS) ws c+=NUMBER)?
                      | ((c+=PLUS | c+=MINUS) ws)? c+=NUMBER)
      -> ^(NTH ^(TERM $a* $b*) ^(TERM $c*));

referenceSeparator:
       ((ws GREATER)=> ws GREATER ws)
       | (ws) -> EMPTY_COMBINATOR;
 
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
    : a=reusableStructureName (ws LPAREN ws b=semiSplitMixinReferenceArguments ws RPAREN ws)? (ws c=IMPORTANT_SYM)?
    -> ^(MIXIN_REFERENCE $a $b* $c*)
    ;
finally { leaveRule(); }

mixinReferenceWithSemi
@init {enterRule(retval, RULE_MIXIN_REFERENCE);}
    : a=reusableStructureName (ws LPAREN ws b=semiSplitMixinReferenceArguments ws RPAREN ws)? (ws c=IMPORTANT_SYM)? SEMI
    -> ^(MIXIN_REFERENCE $a $b* $c*)
    ; 
finally { leaveRule(); }

semiSplitMixinReferenceArguments
    : a+=mixinReferenceArguments ( ws a+=semicolon ws a+=mixinReferenceArguments)* 
    -> ^(SEMI_SPLIT_MIXIN_REFERENCE_ARGUMENTS $a*)
    ;

semicolon
    :SEMI;
    
mixinReferenceArguments
    : // nothing
    | variabledeclarationNoSemi (ws! COMMA! ws! variabledeclarationNoSemi)*
    | expression_full (ws! COMMA! ws! variabledeclarationNoSemi)*
    ;

reusableStructureName
    : a+=cssClassOrId -> ^(REUSABLE_STRUCTURE_NAME $a*);
    
//we can loose parentheses, because comments inside mixin definition are going to be lost anyway
reusableStructure 
@init {enterRule(retval, RULE_ABSTRACT_MIXIN_OR_NAMESPACE);}
    : a=reusableStructureName ws LPAREN ws c=semiSplitReusableStructureArguments ws RPAREN (ws e=reusableStructureGuards)? ws f=general_body
    -> ^(REUSABLE_STRUCTURE $a $c* $e* $f)
    ;
finally { leaveRule(); }

reusableStructureGuards
    : IDENT_WHEN ws b+=guard (ws COMMA ws d+=guard)*
    -> $b $d*
    ;
    
guard
    : a=guardCondition  (ws b+=ident ws c+=guardCondition)*
    -> ^(GUARD $a* ($b $c)*)
    ;    

guardCondition
    : (a+=ident ws)? LPAREN ws b+=mathExprHighPrior (ws b+=compareOperator ws b+=mathExprHighPrior)? ws RPAREN
    -> ^(GUARD_CONDITION $a* $b*)
    ;   

compareOperator
    : GREATER | GREATER_OR_EQUAL | OPEQ | LOWER_OR_EQUAL | LOWER
    ;     
    
semiSplitReusableStructureArguments
    : (    // nothing 
           | a+=commaSplitReusableStructureArgument (ws a+=semicolon ws a+=commaSplitReusableStructureArgument)* (ws a+=semicolon)?
      ) -> ^(SEMI_SPLIT_MIXIN_DECLARATION_ARGUMENTS $a*)
    ;

commaSplitReusableStructureArgument
    : sequenceOfReusableStructureArgumentsWithDefault (ws! COMMA! ws! collector)?
    | patternAndNoDefaultOnlyReusableStructureArguments (ws! COMMA! ws! sequenceOfReusableStructureArgumentsWithDefault (ws! COMMA! ws! collector)?)?   
    ;
    
sequenceOfReusableStructureArgumentsWithDefault
    : reusableStructureParameterWithDefault (ws! COMMA! ws! reusableStructureParameterWithDefault)*
    ;
    
patternAndNoDefaultOnlyReusableStructureArguments
    : (reusableStructureParameterWithoutDefault | reusableStructurePattern) (ws! COMMA! ws! (reusableStructureParameterWithoutDefault | reusableStructurePattern))*
    ;    

reusableStructurePattern
    : (( (a+=plusOrMinus ws)? (a+=value_term)
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
    : property ws COLON (ws expression_full)? -> ^(DECLARATION property COLON expression_full?)
    ;
finally { leaveRule(); }

//I had to do this to put semicolon as a last member of the declaration subtree - comments would be messed up otherwise.
declarationWithSemicolon
@init {enterRule(retval, RULE_DECLARATION);}
    : property ws COLON (ws expression_full)? ws SEMI -> ^(DECLARATION property COLON expression_full?)
    ;
finally { leaveRule(); }
    
prio
    : IMPORTANT_SYM -> ^(EXPRESSION ^(TERM ^(IDENT_TERM IMPORTANT_SYM)))
    ;

operator
    : (ws COMMA ws)=>(ws a+=COMMA ws) -> $a
    | (mandatory_ws)=>(mandatory_ws)-> EMPTY_SEPARATOR
    ;

operator_fictional
    : ws -> EMPTY_SEPARATOR
    ;
    
mathOperatorHighPrior
    : SOLIDUS //ratio in pure CSS
    | STAR
    ;

plusOrMinus
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
    : a+=AT_NAME (ws b+=LPAREN ws b+=RPAREN)? ws SEMI
    -> ^(DETACHED_RULESET_REFERENCE ^(VARIABLE_REFERENCE $a* ) $b*)
    ;
finally { leaveRule(); } 

expression_full
@init {enterRule(retval, RULE_EXPRESSION);}
    : ( (a+=mathExprHighPriorNoWhitespaceList (
            (operator mathExprHighPriorNoWhitespaceList)=>(b+=operator c+=mathExprHighPriorNoWhitespaceList)
            | (term_no_preceeding_whitespace)=>(b+=operator_fictional c+=term_no_preceeding_whitespace)
         )* (b+=operator_fictional c+=prio)? 
      )) -> ^(EXPRESSION $a* ($b $c)* )
    ;
finally { leaveRule(); }

        


/*
No: 1 +1 
Yes: 1+1
Yes: 1+ 1
Yes: 1 + 1
Yes: 1+-1
        (ws plusOrMinus)=>(ws b+=plusOrMinus ws c+=mathExprLowPrior)
*/
mathExprHighPriorNoWhitespaceList
@init {enterRule(retval, RULE_EXPRESSION);}
    : (dr+=detachedRuleset 
      |(a=mathExprLowPrior (
        (plusOrMinus ws mathExprLowPrior)=>(b+=plusOrMinus ws c+=mathExprLowPrior)
        | (plusOrMinus mandatory_ws mathExprLowPrior)=>(b+=plusOrMinus mandatory_ws c+=mathExprLowPrior)
        | (mandatory_ws plusOrMinus mandatory_ws mathExprLowPrior)=>(mandatory_ws b+=plusOrMinus mandatory_ws c+=mathExprLowPrior)
        | (ws plusOrMinus ws plusOrMinus ws mathExprLowPrior)=>(ws b+=plusOrMinus ws c+=mathExprLowPrior)
      )*)) -> ^(EXPRESSION $dr* $a* ($b $c)*)
    ;
finally { leaveRule(); }

mathExprHighPrior
@init {enterRule(retval, RULE_EXPRESSION);}
    : a=mathExprLowPrior (
        (ws plusOrMinus)=>(ws b+=plusOrMinus ws c+=mathExprLowPrior)
      )* -> ^(EXPRESSION $a ($b $c)*)
    ;
finally { leaveRule(); }

mathExprLowPrior
@init {enterRule(retval, RULE_EXPRESSION);}
    : a=term (ws b+=mathOperatorHighPrior ws c+=term)* -> ^(EXPRESSION $a ($b $c)*)
    ;
finally { leaveRule(); }

term
    : ( (
      ((plusOrMinus ws)? function) => ((a+=plusOrMinus ws)? a+=function)
    ) | ( (a+=plusOrMinus ws)? ( 
            a+=value_term
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

term_no_preceeding_whitespace
    : (   a+=variablereference
        | a+=expr_in_parentheses
        | a+=unsigned_value_term 
        | a+=hexColor
        | a+=escapedValue
        | a+=embeddedScript
        | a+=special_function)
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
    : LPAREN ws expression_full ws RPAREN -> ^(EXPRESSION_PARENTHESES LPAREN expression_full RPAREN );
    
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
    : (a+=ident | b+=EXCLAMATION_MARK | b+=DOT | b+=IMPORTANT_SYM) ((identifierValueTermHelper)=>c+=identifierValueTermHelper)* 
          -> ^(IDENT_TERM $a* $b* $c*);
          
identifierValueTermHelper
    : DOT | HASH | ident | EXCLAMATION_MARK | IMPORTANT_SYM
    ;

special_function
    : URI | URL_PREFIX | DOMAIN 
    ;

hexColor
    : HASH -> HASH
    ;

function
    : a=functionName LPAREN ws (b=functionParameters ws)? RPAREN -> ^(TERM_FUNCTION $a* $b*)
    ;

//function names should allow filters: progid:DXImageTransform.Microsoft.gradient(startColorstr='#FF0000ff', endColorstr='#FFff0000', GradientType=1)
functionName
    : (a+=ident (b+=COLON c+=ident| b+=DOT c+=ident)*
    | f+=PERCENT)
    -> ^(TERM_FUNCTION_NAME $a* ($b $c)* $f*)
    ;
    
functionParameters
    : a=namedFunctionParameter (ws b+=COMMA ws c+=namedFunctionParameter)* -> ^(EXPRESSION $a ($b $c)*)
    | expression_full;


namedFunctionParameter
    : ident ws OPEQ ws term -> ^(NAMED_EXPRESSION ident term);
    
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

fragment NONASCII : ( '\u00B0'..'\uFFFF') ; // NB: Upper bound should be \u4177777| ('\u0080'..'\u009F')  

//fragment U+00A0

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

// NOTE: I removed the "whitespace and newlines are unimportant even within keywords" 
// part cause that is not true anymore in less
                        
// Basic Alpha characters in upper, lower and escaped form. Note that
// whitespace and newlines are unimportant even within keywords. We do not
// however call a further fragment rule to consume these characters for
// reasons of performance - the rules are still eminently readable.
//
fragment A : ('a'|'A') 
                | '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'1'
                ;
fragment B : ('b'|'B') 
                | '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'2'
                ;
fragment C : ('c'|'C') 
                | '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'3'
                ;
fragment D : ('d'|'D') 
                | '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'4'
                ;
fragment E : ('e'|'E') 
                | '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'5'
                ;
fragment F : ('f'|'F') 
                | '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'6'
                ;
fragment G : ('g'|'G') 
                | '\\'
                        (
                              'g'
                            | 'G'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'7'
                        )
                ;
fragment H : ('h'|'H') 
                | '\\'
                        (
                              'h'
                            | 'H'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'8'
                        )
                ;
fragment I : ('i'|'I') 
                | '\\'
                        (
                              'i'
                            | 'I'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'9'
                        )
                ;
fragment J : ('j'|'J') 
                | '\\'
                        (
                              'j'
                            | 'J'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('A'|'a')
                        )
                ;
fragment K : ('k'|'K') 
                | '\\'
                        (
                              'k'
                            | 'K'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('B'|'b')
                        )
                ;
fragment L : ('l'|'L') 
                | '\\'
                        (
                              'l'
                            | 'L'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('C'|'c')
                        )
                ;
fragment M : ('m'|'M') 
                | '\\'
                        (
                              'm'
                            | 'M'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('D'|'d')
                        )
                ;
fragment N : ('n'|'N') 
                | '\\'
                        (
                              'n'
                            | 'N'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('E'|'e')
                        )
                ;
fragment O : ('o'|'O') 
                | '\\'
                        (
                              'o'
                            | 'O'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('F'|'f')
                        )
                ;
fragment P : ('p'|'P') 
                | '\\'
                        (
                              'p'
                            | 'P'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('0')
                        )
                ;
fragment Q : ('q'|'Q') 
                | '\\'
                        (
                              'q'
                            | 'Q'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('1')
                        )
                ;
fragment R : ('r'|'R') 
                | '\\'
                        (
                              'r'
                            | 'R'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('2')
                        )
                ;
fragment S : ('s'|'S') 
                | '\\'
                        (
                              's'
                            | 'S'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('3')
                        )
                ;
fragment T : ('t'|'T') 
                | '\\'
                        (
                              't'
                            | 'T'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('4')
                        )
                ;
fragment U : ('u'|'U') 
                | '\\'
                        (
                              'u'
                            | 'U'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('5')
                        )
                ;
fragment V : ('v'|'V') 
                | '\\'
                        ( 'v'
                            | 'V'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('6')
                        )
                ;
fragment W : ('w'|'W') 
                | '\\'
                        (
                              'w'
                            | 'W'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('7')
                        )
                ;
fragment X : ('x'|'X') 
                | '\\'
                        (
                              'x'
                            | 'X'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('8')
                        )
                ;
fragment Y : ('y'|'Y') 
                | '\\'
                        (
                              'y'
                            | 'Y'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('9')
                        )
                ;
fragment Z : ('z'|'Z') 
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
fragment COMMENT_CONTENT : '/*' ( options { greedy=false; } : .*) '*/';
COMMENT : content=COMMENT_CONTENT { $channel = HIDDEN; };

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
GREATER_OR_EQUAL : '>=';
LOWER : '<' ;
LOWER_OR_EQUAL : '=<' | '<=';
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
EXCLAMATION_MARK: '!';
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
IDENT_NOT: N O T;
IDENT_EXTEND: E X T E N D;
IDENT_NTH_CHILD: N T H MINUS C H I L D; 
NTH_LAST_CHILD: N T H MINUS L A S T MINUS C H I L D;
NTH_OF_TYPE: N T H MINUS O F MINUS T Y P E;
NTH_LAST_OF_TYPE: N T H MINUS L A S T MINUS O F MINUS T Y P E;

IDENT_WHEN : W H E N;
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

fragment AT_DOCUMENT : ;
fragment AT_KEYFRAMES: ;
fragment AT_VIEWPORT: ;
fragment AT_SUPPORTS: ;
fragment AT_CHARSET: ;

AT_NAME : '@' NAME { $type = lexerLogic.atNameType(getText()); };
INDIRECT_VARIABLE : '@' '@' NAME ;
INTERPOLATED_VARIABLE : '@' LBRACE NAME RBRACE;

IMPORTANT_SYM : '!' (WS_FRAGMENT|COMMENT_CONTENT)* I M P O R T A N T ;

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
fragment ANGLE :; // 'deg', 'rad', 'grad', 'turn'
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

            | (T U R N)=>
                T U R N { $type = ANGLE; }
            
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
fragment UNICODE_NON_BREAKING_WS: '\u00A0'; 
fragment WS_FRAGMENT : (' '|'\t'|'\f'|UNICODE_NON_BREAKING_WS)+ ;   //('\r'|'\n'|'\t'|'\f'|' ')
WS : WS_FRAGMENT ;//{ $channel = HIDDEN; } ;
fragment NL : ('\r' '\n'? | '\n');
NEW_LINE: NL ;//{ $channel = HIDDEN; } ;

ws: (WS | NEW_LINE)*;
mandatory_ws: (WS | NEW_LINE)+;
// -------------
// Illegal. Any other character shoudl not be allowed.
//


