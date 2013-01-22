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
  NTH;
  PSEUDO;
  ATTRIBUTE;
  ID_SELECTOR;
  ELEMENT_SUBSEQUENT;
  CHARSET_DECLARATION;
  TERM_FUNCTION;
  TERM;
  MEDIUM_DECLARATION;
  MEDIA_EXPRESSION;
  MEDIA_QUERY;
  MEDIUM_TYPE;
  BODY;
  MIXIN_REFERENCE;
  NAMESPACE_REFERENCE;
  REUSABLE_STRUCTURE;
  MIXIN_PATTERN;
  GUARD_CONDITION;
  GUARD;
  DUMMY_MEANINGFULL_WHITESPACE; //ANTLR, please
  KEYFRAMES_DECLARATION;
  KEYFRAMES;
  VIEWPORT;
  REUSABLE_STRUCTURE_NAME;
  PSEUDO_PAGE;
  PAGE_MARGIN_BOX;
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
            return Token.EOF_TOKEN;
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
    : ( a+=charSet* //the original ? was replaced by *, because it is possible (even if it makes no sense) and less.js is able to handle such situation
        a+=bodylist
        EOF ) -> ^(STYLE_SHEET ($a)*)
    ;
finally { leaveRule(); }
    
// -----------------
// Character set. Picks up the user specified character set, should it be present.
// Removed an empty option from this rule, ANTLR seems to have problems with this.
// https://developer.mozilla.org/en/CSS/@charset
charSet
@init {enterRule(retval, RULE_CHARSET);}
    : CHARSET_SYM STRING SEMI -> ^(CHARSET_DECLARATION STRING)
    ;
finally { leaveRule(); }

// ---------
// Import. Location of an external style sheet to include in the ruleset.
//
imports
@init {enterRule(retval, RULE_IMPORTS);}
    : (IMPORT_SYM | IMPORT_ONCE_SYM | IMPORT_MULTIPLE_SYM)^ (term) (mediaQuery (COMMA mediaQuery)*)? SEMI!
    ;
finally { leaveRule(); }

// ---------
// Media. Introduce a set of rules that are to be used if the consumer indicates
// it belongs to the signified medium.
// Media can also have a ruleset in them.
//
//TODO media part of the grammar is throwing away tokens, so comments will not work correctly. fix that
media
@init {enterRule(retval, RULE_MEDIA);}
    : MEDIA_SYM (m1+=mediaQuery (n+=COMMA m+=mediaQuery)*)
        q1=LBRACE
            ((declaration) => b+=declaration SEMI
            | b+=bodyset )*
        q2=RBRACE
    -> ^(MEDIA_SYM ^(MEDIUM_DECLARATION $m1 ($n $m)*) $q1 $b* $q2)
    ;
finally { leaveRule(); }

keyframes
@init {enterRule(retval, RULE_KEYFRAME);}
    : {predicates.isKeyframes(input.LT(1))}? (AT_NAME (name+=IDENT (name+=COMMA name+=IDENT )*)?)
      body+=general_body
    -> ^(KEYFRAMES AT_NAME ^(KEYFRAMES_DECLARATION $name*) $body )
    ;
finally { leaveRule(); }

viewport
@init {enterRule(retval, RULE_KEYFRAME);}
    : {predicates.isViewport(input.LT(1))}? AT_NAME
      body+=general_body
    -> ^(VIEWPORT AT_NAME $body )
    ;
finally { leaveRule(); }

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
    : LPAREN a+=mediaFeature (b+=COLON c+=expr)? RPAREN -> ^(MEDIA_EXPRESSION $a* $b* $c*)
    ;

mediaFeature
    : IDENT
    ;

bodylist
    : bodyset*
    ;
    
bodyset
    : (mixinReferenceWithSemi)=>mixinReferenceWithSemi
    | (namespaceReferenceWithSemi)=>namespaceReferenceWithSemi
    | (reusableStructureName LPAREN)=>reusableStructure
    | (variabledeclaration)=>variabledeclaration
    | ruleSet
    | media
    | viewport
    | keyframes
    | page
    | fontface
    | imports
    ;

variabledeclaration
@init {enterRule(retval, RULE_VARIABLE_DECLARATION);}
    : AT_NAME COLON (a+=expr) SEMI -> ^(VARIABLE_DECLARATION AT_NAME COLON $a* SEMI)
    ;
finally { leaveRule(); }

//used in mixinReferenceArgument
variabledeclarationLimitedNoSemi
@init {enterRule(retval, RULE_VARIABLE_DECLARATION);}
    : AT_NAME COLON (a+=exprNoComma) -> ^(VARIABLE_DECLARATION AT_NAME COLON $a* )
    ;
finally { leaveRule(); }

//This looks like the declaration, but does not allow a comma.
reusableStructureParameter
    : AT_NAME ((b=COLON (a+=exprNoComma)) | b=DOT3)? -> ^(ARGUMENT_DECLARATION AT_NAME $b* $a*)
    ;

variablereference
@init {enterRule(retval, RULE_VARIABLE_REFERENCE);}
    : AT_NAME | INDIRECT_VARIABLE
    ;
finally { leaveRule(); }

fontface
@init {enterRule(retval, RULE_FONT_FACE);}
    : FONT_FACE_SYM^
        LBRACE!
            declaration SEMI! (declaration SEMI!)*
        RBRACE!
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
    
//combinator
//    : GREATER
//    | PLUS
//    | TILDE
//    | ( -> EMPTY_COMBINATOR)
//    ;

combinator
    : GREATER
    | PLUS
    | TILDE
    | ({predicates.onEmptyCombinator(input)}?)=> -> EMPTY_COMBINATOR
    ;
    
unaryOperator
    : MINUS
    | PLUS
    ;
    
property
    : {predicates.directlyFollows(input.LT(1), input.LT(2))}?=> STAR IDENT // support for star prefix
    | IDENT // normal property
    ;
    
//we need to put comma into the tree so we can collect comments to it
//TODO: this does not accurately describes the grammar. Nested and real selectors are different.
ruleSet
@init {enterRule(retval, RULE_RULESET);}
    : (a+=selector ( a+=selectorSeparator a+=selector)*)?
       b=general_body
     -> ^(RULESET $a* $b)
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
general_body
    : LBRACE
            (   ((declarationWithSemicolon)=> (a+=declarationWithSemicolon) )
               | (ruleSet)=> a+=ruleSet
               | (mixinReferenceWithSemi)=>a+=mixinReferenceWithSemi
               | (namespaceReferenceWithSemi)=>a+=namespaceReferenceWithSemi
               | (reusableStructure)=>a+=reusableStructure
               | a+=pageMarginBox 
               | a+=variabledeclaration
               //| a+=imports
             )*
             (  
                ( (declaration)=>a+=declaration RBRACE)
                | ( (mixinReference)=>a+=mixinReference RBRACE)
                | ( (namespaceReference)=>a+=namespaceReference RBRACE)
                | RBRACE
             )
     //If we remove LBRACE from the tree, a ruleset with an empty selector will report wrong line number in the warning.
     -> ^(BODY LBRACE $a*); 

selector 
@init {enterRule(retval, RULE_SELECTOR);}
    : ((combinator)=>a+=combinator | ) 
      (a+=simpleSelector | a+=nestedAppender | a+=escapedSelectorOldSyntax) 
      ( 
        ((combinator)=>a+=combinator | ) 
        (a+=simpleSelector | a+=nestedAppender | a+=escapedSelectorOldSyntax)
      )*
    -> ^(SELECTOR $a* ) 
    ;
finally { leaveRule(); }

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
    : DOT a+=idOrClassNamePart ({predicates.directlyFollows(input.LT(-1), input.LT(1))}?=>a+=idOrClassNamePart)* -> ^(CSS_CLASS $a*); 
    
idOrClassNamePart
    : IDENT | MINUS | allNumberKinds | INTERPOLATED_VARIABLE;
    
elementName
    :  a+=elementNamePart ({predicates.directlyFollows(input.LT(-1), input.LT(1))}?=>a+=elementNamePart)* -> ^(ELEMENT_NAME $a*);
    
elementNamePart
    : IDENT | MINUS | STAR | allNumberKinds | INTERPOLATED_VARIABLE;
        
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
                      a+=IDENT
                    | a+=STRING
                    | a+=NUMBER
                )
            )?
    
      RBRACKET)
      -> ^(ATTRIBUTE $a* )
;

pseudo
    : (c+=COLON c+=COLON? a=IDENT ((
        { predicates.insideNth(input)}?=> LPAREN (b1=nth| b2=variablereference) RPAREN
        | LPAREN b3=pseudoparameters RPAREN
        )?)
      ) -> ^(PSEUDO $c+ $a $b1* $b2* $b3*)
    ;
    
pseudoparameters:
      (IDENT) => IDENT
    | (NUMBER) => NUMBER
    | (variablereference) => variablereference
    | selector
 ;

//TODO: add special error message here 
 nth: ((a+=PLUS | a+=MINUS)? (a+=REPEATER | a+=IDENT) ((b+=PLUS | b+=MINUS) b+=NUMBER)?
                      | (b+=PLUS | b+=MINUS)? b+=NUMBER)
      -> ^(NTH ^(TERM $a*) ^(TERM $b*));

referenceSeparator:
       GREATER
       | {!predicates.directlyFollows(input.LT(-1), input.LT(1))}?=> -> EMPTY_COMBINATOR;
 
namespaceReference
@init {enterRule(retval, RULE_NAMESPACE_REFERENCE);}
    : ((reusableStructureName referenceSeparator)=> a+=reusableStructureName referenceSeparator)+
      c+=mixinReferenceWithSemi -> ^(NAMESPACE_REFERENCE $a* $c);
finally { leaveRule(); }

namespaceReferenceWithSemi
@init {enterRule(retval, RULE_NAMESPACE_REFERENCE);}
    : ((reusableStructureName referenceSeparator)=> a+=reusableStructureName referenceSeparator)+
      c+=mixinReference SEMI -> ^(NAMESPACE_REFERENCE $a* $c);
finally { leaveRule(); }

mixinReference
@init {enterRule(retval, RULE_MIXIN_REFERENCE);}
    : a=reusableStructureName (LPAREN b=mixinReferenceArguments? RPAREN)? c=IMPORTANT_SYM?
    -> ^(MIXIN_REFERENCE $a $b* $c*)
    ;
finally { leaveRule(); }

mixinReferenceWithSemi
@init {enterRule(retval, RULE_MIXIN_REFERENCE);}
    : a=reusableStructureName (LPAREN b=mixinReferenceArguments? RPAREN)? c=IMPORTANT_SYM? SEMI
    -> ^(MIXIN_REFERENCE $a $b* $c*)
    ; 
finally { leaveRule(); }

mixinReferenceArguments
    : a+=mixinReferenceArgument ( COMMA a+=mixinReferenceArgument)*
    -> $a*
    ;

mixinReferenceArgument
    : exprNoComma | variabledeclarationLimitedNoSemi
    ;

//FIXME: add additional check - these should NOT trigger interpolation    
reusableStructureName
    : a+=cssClassOrId ({predicates.directlyFollows(input.LT(-1), input.LT(1))}?=> a+=cssClassOrId)* -> ^(REUSABLE_STRUCTURE_NAME $a*);
    
//we can loose parentheses, because comments inside mixin definition are going to be lost anyway
reusableStructure 
@init {enterRule(retval, RULE_ABSTRACT_MIXIN_OR_NAMESPACE);}
    : a=reusableStructureName LPAREN c=reusableStructureArguments? RPAREN e=reusableStructureGuards? f=general_body
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
    
//It is OK to loose commas, because mixins are going to be lost anyway    
reusableStructureArguments
    : a+=reusableStructureArgument ( COMMA a+=reusableStructureArgument)* (COMMA b+=DOT3)?
    -> $a* $b*
    | DOT3
    ;

reusableStructureArgument
    : reusableStructureParameter
    | reusableStructurePattern
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

expr 
@init {enterRule(retval, RULE_EXPRESSION);}
    : a=mathExprHighPrior (b+=operator c+=mathExprHighPrior)* -> ^(EXPRESSION $a ($b $c)*)
    ;
finally { leaveRule(); }

exprNoComma
@init {enterRule(retval, RULE_EXPRESSION);}
    : a=mathExprHighPrior (b+=operatorNoComma c+=mathExprHighPrior)* -> ^(EXPRESSION $a ($b $c)*)
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
        | a+=special_function))
    -> ^(TERM $a*)
    ;

escapedValue: VALUE_ESCAPE -> ^(ESCAPED_VALUE VALUE_ESCAPE);
    
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
    | IDENT
    ;

special_function
    : URI
    ;

hexColor
    : HASH -> HASH
    ;

function
    : (a=IDENT | a=PERCENT) LPAREN b=functionParameter RPAREN -> ^(TERM_FUNCTION $a $b*)
    ;
    
functionParameter
    : IDENT OPEQ term -> ^(OPEQ IDENT term)
    | expr;
    
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

fragment NMSTART : '_'
                        | 'a'..'z'
                        | 'A'..'Z'
                        | NONASCII
                        | ESCAPE
                        ;

fragment NMCHAR : '_'
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
                              '['|'!'|'#'|'$'|'%'|'&'|'*'|'~'|'/'|'.'|'='|':'|';'|','|'\r'|'\n'|'\t'|' '|'+'
                            | NMCHAR
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
SEMI : ';' ;
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
                
// -------------
// Identifier. Identifier tokens pick up properties names and values
//
//
IDENT : '-'? NMSTART NMCHAR* ;

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
CHARSET_SYM : '@charset ' ;

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
// url and uri.
URI : (U R L
        '('
            ((WS)=>WS)? (URL) WS?
        ')') => (U R L
        '('
            ((WS)=>WS)? (URL) WS?
        ')')
        | U R L { $type=IDENT; }
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


