/*
 [The "BSD licence"]
 Copyright (c) 2013 Terence Parr
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

grammar LessG4;

ident_nth: IDENT_NTH_CHILD | NTH_LAST_CHILD | NTH_OF_TYPE | NTH_LAST_OF_TYPE;
ident_keywords: IDENT_WHEN;

ident_special_pseudoclasses: ident_nth | IDENT_EXTEND;

ident: IDENT | IDENT_NOT | ident_keywords | ident_special_pseudoclasses;
ident_except_not: IDENT | ident_keywords | ident_special_pseudoclasses;
ident_general_pseudo: IDENT | IDENT_NOT | ident_keywords;


styleSheet : ws (top_level_element ws)* EOF;

//top_level_element
//    : 
//    (mixinReferenceWithSemi)=>mixinReferenceWithSemi
//    | (namespaceReferenceWithSemi)=>namespaceReferenceWithSemi
//    | (reusableStructureName ws LPAREN)=>reusableStructure
//    | (detachedRulesetReference)=>detachedRulesetReference
//    | (variabledeclaration)=>variabledeclaration
//    | ruleSet
//    | media_top_level
//    | viewport
//    | keyframes
//    | page
//    | fontface
//    | imports
//    | document
//    | supports
//    | charSet
//    | unknownAtRule
//    ;

top_level_element
    : mixinReferenceWithSemi
    | namespaceReferenceWithSemi
    | reusableStructure
    | detachedRulesetReference
    | variabledeclarationWithSemicolon
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

top_level_body: LBRACE (ws a+=top_level_element)* ws RBRACE;

top_level_body_with_declaration: LBRACE ( ws | (
                ws top_level_body_with_declaration_member
            )+) ws
      RBRACE;
      
top_level_body_with_declaration_member:  declarationWithSemicolon | top_level_element;

fontface: FONT_FACE_SYM ws body=general_body;

page: PAGE_SYM (ws ident)? (ppDock=mandatory_ws? pseudoPage)? ws general_body;

pageMarginBox: AT_PAGE_MARGIN_BOX ws general_body;

pseudoPage: COLON ws ident;
    
// -----------------
// Character set. Picks up the user specified character set, should it be present.
// Removed an empty option from this rule, ANTLR seems to have problems with this.
// https://developer.mozilla.org/en/CSS/@charset
charSet: AT_CHARSET ws STRING ws SEMI;

// ---------
// Import. Location of an external style sheet to include in the ruleset.
//
imports: (kind=IMPORT_SYM | kind=IMPORT_ONCE_SYM | kind=IMPORT_MULTIPLE_SYM) ws
          (importoptions ws)? 
          url=term 
          (mandatory_ws mediaQuery ws (COMMA ws mediaQuery ws)*)? 
          SEMI;

importoptions: LPAREN (ws option+=ident ws COMMA)* (ws option+=ident)? RPAREN;
// ---------
// Media. Introduce a set of rules that are to be used if the consumer indicates
// it belongs to the signified medium.
// Media can also have a ruleset in them.
//
//TODO media part of the grammar is throwing away tokens, so comments will not work correctly. fix that
media_top_level: MEDIA_SYM ws firstQuery=mediaQuery (ws COMMA ws tail+=mediaQuery)* ws body=top_level_body_with_declaration;

media_in_general_body: MEDIA_SYM ws firstQuery=mediaQuery (ws COMMA ws tail+=mediaQuery)* ws body=general_body;
    
mediaQuery: medium (ws ident ws mediaExpression)*
    | mediaExpression (ws ident ws mediaExpression)*;
    
medium: ident (ws ident)?;    
    
mediaExpression: cssMediaExpression | interpolatedMediaExpression;

cssMediaExpression: LPAREN ws mediaFeature ws (COLON ws expression_full ws)? RPAREN;

interpolatedMediaExpression: variablereference (ws variablereference)*;
    
mediaFeature: ident;

keyframes: AT_KEYFRAMES 
	  (ws names+=keyframesname ( ws commas+=COMMA ws names+=keyframesname )*)?
    ws general_body;

keyframesname: ident | variablereference | STRING;

document: AT_DOCUMENT ws
      urlFnc+=term_only_function (ws c+=COMMA ws urlFnc+=term_only_function)*
      ws body=top_level_body;

viewport: AT_VIEWPORT ws body=general_body;

supports: AT_SUPPORTS ws condition=supportsCondition ws body=general_body;

supportsCondition: first=simpleSupportsCondition 
     (ws oper+=ident_except_not ws second+=simpleSupportsCondition)*
    ;

simpleSupportsCondition:   
       supportsQuery #sscDeclaration 
     | IDENT_NOT ws supportsCondition #sscNotCondition
     | LPAREN ws supportsCondition ws RPAREN #sscNestedCondition
    ;
    
supportsQuery: LPAREN ws declaration ws RPAREN;

unknownAtRule: AT_NAME (ws unknownAtRuleNamesSet)? ws ( body=general_body | semi=SEMI );

unknownAtRuleNamesSet: (mathExprHighPrior) (ws COMMA (ws mathExprHighPrior))*;

ruleSet: leadingSelector=selector? (
	          (ws COMMA a+=selector)* (ws COMMA)?
	        | mandatory_ws reusableStructureGuards
         ) 
         ws general_body
         ;

combinator: GREATER | PLUS | TILDE | HAT | CAT | SOLIDUS ident SOLIDUS;

combinator_ws: ws combinator ws | mandatory_ws;

//FIXME: just guessing something, cause I am not sure what was all that in original supposed to do
selector: leading=combinator_ws? non_combinator_selector_block (combinator_ws non_combinator_selector_block)*;

non_combinator_selector_block: (nestedAppender | simpleSelector | escapedSelectorOldSyntax)+;

escapedSelectorOldSyntax: LPAREN VALUE_ESCAPE RPAREN;
nestedAppender: APPENDER;
simpleSelector: ( 
	      (elementName elementSubsequent*)
        | elementSubsequent+
      )
;

elementName: elementNamePart+;
elementNamePart: STAR | IDENT | MINUS | allNumberKinds | INTERPOLATED_VARIABLE | UNDERSCORE;
allNumberKinds: NUMBER | EMS | EXS | LENGTH | ANGLE | TIME | FREQ | REPEATER | PERCENTAGE | UNKNOWN_DIMENSION;

elementSubsequent: cssClassOrId | attribOrPseudo;
cssClassOrId: idSelector | cssClass;
attribOrPseudo: attrib | pseudo;
    
//A class name can be also a number e.g., .56 or .5cm or anything else that starts with a dot '.'.
cssClass: DOT (fixedIdOrClassNamePart| INTERPOLATED_VARIABLE)+; 
idSelector: (HASH | HASH_SYMBOL INTERPOLATED_VARIABLE) (fixedIdOrClassNamePart| INTERPOLATED_VARIABLE)*;
fixedIdOrClassNamePart: ident | MINUS | allNumberKinds;

selectorAttributeOperator: OPEQ | INCLUDES | DASHMATCH | PREFIXMATCH | SUFFIXMATCH | SUBSTRINGMATCH; 
attrib: LBRACKET ws ident ( ws selectorAttributeOperator term)? ws RBRACKET;

pseudo
    : (COLON COLON? ( 
          (ident_nth LPAREN ws (nth | variablereference | INTERPOLATED_VARIABLE) ws RPAREN)
        | (IDENT_EXTEND LPAREN ws (extendTargetSelectors) ws RPAREN)
        | (IDENT_NOT LPAREN ws (selector) ws RPAREN)
        | (ident_general_pseudo LPAREN ws (
        	pseudoparameter_termValue ws RPAREN
        	| selector ws RPAREN
        ))
        |  ident
    ));
    
pseudoparameter_termValue: ident | STRING | NUMBER | variablereference;    

//TODO: add special error message here 
nth: nth_base (ws nth_expression)? | nth_expression;
nth_expression:  (sign=plusOrMinus ws)? onlyNumber=NUMBER;
nth_base: (sign=plusOrMinus ws )? (repeater=REPEATER | name=ident);
               

extendTargetSelectors: selector (ws COMMA ws selector)*;

extendInDeclarationWithSemi: APPENDER pseudo SEMI;
  

general_body: LBRACE (ws general_body_member)* (ws noSemiMember=general_body_member_no_semi)? ws RBRACE;
//general_body: LBRACE (ws general_body_member)* (ws general_body_member_no_semi)? ws RBRACE;

general_body_member:
	declarationWithSemicolon
	| ruleSet
	| mixinReferenceWithSemi
	| namespaceReferenceWithSemi
	| reusableStructure
	| detachedRulesetReference
	| variabledeclarationWithSemicolon
    | extendInDeclarationWithSemi
    | pageMarginBox
    | media_in_general_body
    | viewport
    | keyframes
    | document
    | supports
    | page
    | fontface
    | imports
    | unknownAtRule
//               | (ws SEMI)=>(ws SEMI)

;

general_body_member_no_semi:
	declaration
	| mixinReference
	| namespaceReference
	| variabledeclaration
	;

referenceSeparator: ws GREATER ws | ws;
namespaceReference: (reusableStructureName referenceSeparator)+ mixinReference;
namespaceReferenceWithSemi: namespaceReference SEMI;

mixinReference: reusableStructureName (ws LPAREN (ws mixinReferenceArguments)? ws RPAREN ws)? (ws IMPORTANT_SYM)?;
mixinReferenceWithSemi: mixinReference SEMI; 

mixinReferenceArguments: semiSplitMixinReferenceArguments | commaSplitMixinReferenceArguments;
commaSplitMixinReferenceArguments: mixinReferenceArgument_no_comma ( ws COMMA ws mixinReferenceArgument_no_comma)*;
semiSplitMixinReferenceArguments: mixinReferenceArgument_with_comma ( 
	  ws SEMI
	| (ws SEMI ws mixinReferenceArgument_with_comma)+  (ws SEMI)? 
   );

mixinReferenceArgument_with_comma: posValue=expression_full | variablename ws COLON ws namedValue=expression_full;
mixinReferenceArgument_no_comma: posValue=expression_space_separated_list | variablename ws COLON ws namedValue=expression_space_separated_list;

reusableStructureName: cssClassOrId;
    
//we can loose parentheses, because comments inside mixin definition are going to be lost anyway
reusableStructure: reusableStructureName ws 
    LPAREN (ws reusableStructureArguments)? ws RPAREN 
    (ws reusableStructureGuards)? 
    ws general_body;

reusableStructureGuards: IDENT_WHEN ws guard (ws COMMA ws guard)*;

guard: guardCondition  (ws ident ws guardCondition)*;    

guardCondition: (ident ws)? LPAREN ws leftE=mathExprHighPrior (ws compareOperator ws rightE=mathExprHighPrior)? ws RPAREN;   

compareOperator: GREATER | GREATER_OR_EQUAL | OPEQ | LOWER_OR_EQUAL | LOWER;     

reusableStructureArguments: commaSplitReusableStructureArguments | semiSplitReusableStructureArguments;

semiSplitReusableStructureArguments: 
    rsParameter_with_comma (
    	   ws SEMI
    	| (ws SEMI ws rsParameter_with_comma)+ (ws SEMI)? 
    );

commaSplitReusableStructureArguments: 
    rsParameter_no_comma 
    (ws COMMA ws rsParameter_no_comma)* (ws COMMA)?;

rsParameter_no_comma: rsParameterWithoutDefault | rsParameterWithDefault_no_comma;
rsParameter_with_comma: rsParameterWithoutDefault | rsParameterWithDefault_with_comma;  
rsParameterWithDefault_no_comma: AT_NAME ws COLON ws value=expression_space_separated_list;
rsParameterWithDefault_with_comma: AT_NAME ws COLON ws value=expression_full;
rsParameterWithoutDefault: rsAtName | collector | rsPattern;
rsPattern: (( (sign=plusOrMinus ws)? (value_term)
    ) | (
       unsigned_value_term
       | hexColor
    ))
    ;
rsAtName:  AT_NAME (ws DOT3)?;
collector: DOT3;

variabledeclaration: variablename ws COLON (ws expression_full)?;
variabledeclarationWithSemicolon: variabledeclaration ws SEMI;

////The expr is optional, because less.js supports this: "margin: ;" I do not know why, but they have it in
////their unit tests (so it was intentional)
//declaration
//@init {enterRule(retval, RULE_DECLARATION);}
//    : property ws COLON (ws expression_full)? -> ^(DECLARATION property COLON expression_full?)
//    ;
//finally { leaveRule(); }

declaration: 
	property ( | ws PLUS ( | ws UNDERSCORE)) ws COLON (ws expression_full)? 
	;

declarationWithSemicolon: 
	declaration ws SEMI
	;

property: // support for star prefix browser hack - more correct and strict solution would require them to directly follow
          // normal property| merged property 
          (STAR ws)? propertyNamePart+
    ;
    
propertyNamePart
    :  ident | NUMBER | MINUS | INTERPOLATED_VARIABLE;

variablereference: variablename | INDIRECT_VARIABLE;

variablename:
  AT_NAME | AT_PAGE_MARGIN_BOX | IMPORT_SYM | IMPORT_ONCE_SYM | IMPORT_MULTIPLE_SYM | PAGE_SYM | MEDIA_SYM | FONT_FACE_SYM | AT_KEYFRAMES | AT_DOCUMENT | AT_VIEWPORT | AT_SUPPORTS | AT_CHARSET
  ;


plusOrMinus:  PLUS | MINUS;

detachedRuleset: general_body;
detachedRulesetReference: AT_NAME (ws LPAREN ws RPAREN)? ws SEMI;

mathOperatorHighPrior: 
    SOLIDUS //ratio in pure CSS
    | STAR
    ;
 
expression_full: expression_comma_separated_list; //FIXME: what about important?

expression_comma_separated_list: expression_space_separated_list (ws COMMA ws expression_space_separated_list)*;

expression_space_separated_list: mathExprHighPriorNoWhitespaceList (
	          term_no_preceeding_whitespace
	        |  mandatory_ws mathExprHighPriorNoWhitespaceList
        )*;
	
mathExprHighPriorNoWhitespaceList: mathExprLowPrior mathExprHighPriorNoWhitespaceList_helper*;

mathExprHighPriorNoWhitespaceList_helper: operator=plusOrMinus ws value=mathExprLowPrior                
        | operator=plusOrMinus mandatory_ws value=mathExprLowPrior
        | mandatory_ws operator=plusOrMinus mandatory_ws value=mathExprLowPrior
        | ws operator=plusOrMinus ws valueSign=plusOrMinus ws value=mathExprLowPrior
        ;

mathExprHighPrior: mathExprLowPrior (ws plusOrMinus ws mathExprLowPrior)*;

mathExprLowPrior: term (ws mathOperatorHighPrior ws term)*;

term: (( (sign=plusOrMinus ws)? (value_term
    | function
    | expr_in_parentheses
    | variablereference
    )
    ) | ( unsigned_value_term 
        | hexColor
        | escapedValue
        | embeddedScript
        | special_function
        | detachedRuleset))
    ;

term_no_preceeding_whitespace
    : (unsigned_value_term 
        | hexColor
        | escapedValue
        | embeddedScript
        | special_function
        | detachedRuleset)
    ;
  
term_only_function
    : (function
    | special_function)
    ;    

escapedValue: VALUE_ESCAPE;

embeddedScript: EMBEDDED_SCRIPT | ESCAPED_SCRIPT;

expr_in_parentheses: LPAREN ws expression_full ws RPAREN;
    
value_term: 
      NUMBER
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
    
identifierValueTerm: (ident | EXCLAMATION_MARK | DOT | IMPORTANT_SYM) identifierValueTermHelper* ;
          
identifierValueTermHelper: DOT | HASH | ident | EXCLAMATION_MARK | IMPORTANT_SYM;

special_function: URI | URL_PREFIX | DOMAIN;

hexColor: HASH;

function: functionName LPAREN ws (functionParameters ws)? RPAREN;

//function names should allow filters: progid:DXImageTransform.Microsoft.gradient(startColorstr='#FF0000ff', endColorstr='#FFff0000', GradientType=1)
functionName: (
	  ident (COLON ident| DOT ident)*
    | f+=PERCENT)
    ;
    
functionParameters:
      namedFunctionParameter (ws COMMA ws namedFunctionParameter)*
    | expression_full;


namedFunctionParameter: ident ws OPEQ ws term;

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
                                
fragment ESCAPE : UNICODE | '\\' ~[\r\n\f];

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
fragment COMMENT_CONTENT : '/*' .*? '*/';
//FIXME: had to remove multi token stuff
COMMENT : COMMENT_CONTENT WS_FRAGMENT? -> channel(HIDDEN);


//TODO: document: annoying - causes antlr null pointer exception during generation
//catch in lexer is impossible
//COMMENT_LITTLE : '//' ~(NL)* { $channel = HIDDEN; };
//COMMENT_LITTLE : '//' (( options { greedy=false; } : .* ) NL) { $channel = HIDDEN; };
COMMENT_LITTLE : '//' ~[\r\n]* -> channel(HIDDEN);

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
CDO : '<!--'  -> channel(HIDDEN);
    
// ---------------------
// HTML comment close. HTML/XML comments may be placed around style sheets so that they
// are hidden from higher scope parsing engines such as HTML parsers.
// They comment close is therfore ignored by the CSS parser and we hide
// it from the ANLTR parser.
//
CDC : '-->' -> channel(HIDDEN);
                
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
SEMI : ';' (WS_FRAGMENT? ';')* ;
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

//FIXME: had to remove multi emit
APPENDER: '&';

// -----------------
// Literal strings. Delimited by either ' or "
// original grammar considered a single isolated letter to be a string too.
// therefore h1, h2 > a > p, h3 would be tokenized as IDENT COLON IDENT GREATER STRING GREATER STRING ...
// removed that option
//the original string definition did not supported escaping '\\\\'*

//This would normally contains all allowed escape symbols. As we are doing compiler into css, we do not
//care about the exact meaning of escaped symbol, nor we do care whether that make sense.
//we just want to eat a character that follows escape symbol.
fragment ESCAPED_SYMBOL : '\\' . ;
STRING : ('\'' ( ESCAPED_SYMBOL | ~('\n'|'\r'|'\f'|'\\'|'\'') )*? '\'')
         | ('"' ( ESCAPED_SYMBOL | ~('\n'|'\r'|'\f'|'\\'|'"') )*? '"');
VALUE_ESCAPE : '~' '\'' ( ESCAPED_SYMBOL | ~('\n'|'\r'|'\f'|'\\'|'\'') )*? '\''
         | '~' '"' ( ESCAPED_SYMBOL | ~('\n'|'\r'|'\f'|'\\'|'"') )*? '"';
EMBEDDED_SCRIPT : '`' ( ESCAPED_SYMBOL | ~('\f'|'\\'|'`') )*? '`';
ESCAPED_SCRIPT : '~' '`' ( ESCAPED_SYMBOL | ~('\f'|'\\'|'`') )*? '`';
//INVALID: '\'' ( ESCAPED_SYMBOL | ~('\n'|'\r'|'\f'|'\\'|'\'') )* 
//         | '"' ( ESCAPED_SYMBOL | ~('\n'|'\r'|'\f'|'\\'|'\'') )* 
//         | '~' '\'' ( ESCAPED_SYMBOL | ~('\n'|'\r'|'\f'|'\\'|'\'') )*
//         | '~' '"' ( ESCAPED_SYMBOL | ~('\n'|'\r'|'\f'|'\\'|'\'') )* 
//         | '`' ( ESCAPED_SYMBOL | ~('\f'|'\\'|'`') )* 
//         | '~' '`' ( ESCAPED_SYMBOL | ~('\f'|'\\'|'`') )* ;
         
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

AT_DOCUMENT : '@' NMCHAR*? D O C U M E N T;
AT_KEYFRAMES: '@' NMCHAR*? K E Y F R A M E S;
AT_VIEWPORT: '@' NMCHAR*? V I E W P O R T;
AT_SUPPORTS: '@' NMCHAR*? S U P P O R T S;
AT_CHARSET: '@' NMCHAR*? C H A R S E T;

AT_PAGE_MARGIN_BOX: '@' T O P MINUS L E F T MINUS C O R N E R 
	| '@' T O P MINUS L E F T 
	| '@' T O P MINUS C E N T E R 
	| '@' T O P MINUS R I G H T 
	| '@' T O P MINUS R I G H T MINUS C O R N E R 
	| '@' B O T T O M MINUS L E F T MINUS C O R N E R 
	| '@' B O T T O M MINUS L E F T 
	| '@' B O T T O M MINUS C E N T E R 
	| '@' B O T T O M MINUS R I G H T 
	| '@' B O T T O M MINUS R I G H T MINUS C O R N E R 
	| '@' L E F T MINUS T O P 
	| '@' L E F T MINUS M I D D L E 
	| '@' L E F T MINUS B O T T O M 
	| '@' R I G H T MINUS T O P 
	| '@' R I G H T MINUS M I D D L E 
	| '@' R I G H T MINUS B O T T O M ;

AT_NAME : '@' NAME;
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

// there is no reason to require a dot inside a number
fragment PURE_NUMBER: '0'..'9'+ ('.' '0'..'9'+)?
            | '.' '0'..'9'+;
            
EMS:	PURE_NUMBER E M;
EXS:	PURE_NUMBER E X;

LENGTH:  PURE_NUMBER P X // 'px'. 'cm', 'mm', 'in'. 'pt', 'pc'
         | PURE_NUMBER P T
         | PURE_NUMBER P C
         | PURE_NUMBER C M
         | PURE_NUMBER M M
         | PURE_NUMBER I N; 

TIME: PURE_NUMBER M S
      | PURE_NUMBER S; 

ANGLE: PURE_NUMBER D E G // 'deg', 'rad', 'grad'
       | PURE_NUMBER R A D;

FREQ: PURE_NUMBER K? H Z; // 'khz', 'hz'
      
REPEATER: PURE_NUMBER N; // n found in n-th child formulas if I would not do that, the dimension would eat it.
PERCENTAGE: PURE_NUMBER PERCENT;
UNKNOWN_DIMENSION: PURE_NUMBER NMSTART+;
          
NUMBER: PURE_NUMBER;
    
// ------------
// special functions that takes url/uri as a parameter, but do not enclose it in quotes.
URI : U R L '(' WS? URL WS? ')';
URL_PREFIX : U R L MINUS P R E F I X '(' WS? URL WS? ')';
DOMAIN : D O M A I N '(' WS? URL WS? ')';
    
// -------------
// Whitespace. Though the W3 standard shows a Yacc/Lex style parser and lexer
// that process the whitespace within the parser, ANTLR does not
// need to deal with the whitespace directly in the parser.
//
fragment UNICODE_NON_BREAKING_WS: '\u00A0'; 
fragment WS_FRAGMENT : (' '|'\t'|'\f'|UNICODE_NON_BREAKING_WS)+ ;   //('\r'|'\n'|'\t'|'\f'|' ')
WS : WS_FRAGMENT ;//{ $channel = HIDDEN; } ;
fragment NL : ('\r' '\n'? | '\n');
NEW_LINE: WS_FRAGMENT* NL WS_FRAGMENT*;//{ $channel = HIDDEN; } ;

ws: (WS | NEW_LINE)*;
mandatory_ws: (WS | NEW_LINE)+;
