//http://stackoverflow.com/questions/7765018/returning-list-in-antlr-for-type-checking-language-java
//
// A complete lexer and grammar for CSS 2.1 as defined by the
// W3 specification.
//
// This grammar is free to use providing you retain everyhting in this header comment
// section.
//
// Author      : Jim Idle, Temporal Wave LLC.
// Contact     : jimi@temporal-wave.com
// Website     : http://www.temporal-wave.com
// License     : ANTLR Free BSD License
//
// Please visit our Web site at http://www.temporal-wave.com and try our commercial
// parsers for SQL, C#, VB.Net and more.
//
// This grammar is free to use providing you retain everything in this header comment
// section.
//
grammar Less;

options {
    // antlr will generate java lexer and parser
    language = Java;
    // generated parser should create abstract syntax tree
    output = AST;
}

//this is not official way how to do things, but debugging 
//stuff when parsing functionality is not all written in
//parser rules is much easier. 
tokens {  
//  VARIABLE_DECLARATION;
  EXPRESSION;
  DECLARATION;
//  VARIABLE_REFERENCE;
  RULESET;
  SELECTOR;
  EXPRESSION;
  STYLE_SHEET;
  //ANTLR seems to generate null pointers exceptions on malformed css if 
  //rules match nothig and generate an empty token 
  EMPTY_SEPARATOR; 
  ELEMENT_NAME;
  CSS_CLASS;
  SIMPLE_SELECTOR;
  PSEUDO;
  ATTRIBUTE;
  ID_SELECTOR;
  CHARSET_DECLARATION;
  TERM_FUNCTION;
  TERM;
  MEDIUM_DECLARATION;
}  

@lexer::header {
  package org.porting.less4j.core.parser;
}
 
@parser::header {
  package org.porting.less4j.core.parser;
  
  import org.porting.less4j.core.parser.ILessGrammarCallback;
}

//override some methods and add new members to generated lexer
@lexer::members {
  List tokens = new ArrayList();
  public void emit(Token token) {
        state.token = token;
        tokens.add(token);
  }
  public Token nextToken() {
        super.nextToken();
        if ( tokens.size()==0 ) {
            return Token.EOF_TOKEN;
        }
        return (Token)tokens.remove(0);
  }
  //add new field
  private List<RecognitionException> errors = new ArrayList<RecognitionException>();
  
  //add new method
  public List<RecognitionException> getAllErrors() {
    return new ArrayList<RecognitionException>(errors);
  }

  //add new method
  public boolean hasErrors() {
    return !errors.isEmpty();
  }
  
  //override method
  public void reportError(RecognitionException e) {
    errors.add(e);
    displayRecognitionError(this.getTokenNames(), e);
  }
  
}

//override some methods and add new members to generated parser
@parser::members {
  //add new field
  private ILessGrammarCallback grammarCallback = ILessGrammarCallback.NULL_CALLBACK;
  private List<RecognitionException> errors = new ArrayList<RecognitionException>();
  
  public LessParser(ILessGrammarCallback grammarCallback, TokenStream input) {
    this(input);
    this.grammarCallback=grammarCallback;
  }

  public LessParser(ILessGrammarCallback grammarCallback, TokenStream input, RecognizerSharedState state) {
    super(input, state);
    this.grammarCallback=grammarCallback;
  }
  
  //add new method
  public ILessGrammarCallback getGrammarCallback() {
    return grammarCallback;
  }

  public void setGrammarCallback(ILessGrammarCallback grammarCallback) {
    this.grammarCallback = grammarCallback;
  }
  
  public List<RecognitionException> getAllErrors() {
    return new ArrayList<RecognitionException>(errors);
  }

  //add new method
  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  //override method
  public void reportError(RecognitionException e) {
    errors.add(e);
    displayRecognitionError(this.getTokenNames(), e);
  }
  
}

// -------------
// Main rule.   This is the main entry rule for the parser, the top level
//              grammar rule.
//
// A style sheet consists of an optional character set specification, an optional series
// of imports, and then the main body of style rules.
//
styleSheet  
    :  ( a+=charSet* //the original ? was replaced by *, because it is possible (even if it makes no sense) and less.js is able to handle such situation
        a+=imports*
        a+=bodylist
        EOF ) -> ^(STYLE_SHEET ($a)*)
    ;
    
// -----------------
// Character set.   Picks up the user specified character set, should it be present.
// Removed an empty option from this rule, ANTLR seems to have problems with this.
// https://developer.mozilla.org/en/CSS/@charset
charSet
    :   CHARSET_SYM STRING SEMI -> ^(CHARSET_DECLARATION STRING)
    ;

// ---------
// Import.  Location of an external style sheet to include in the ruleset.
//
imports
    :   IMPORT_SYM (STRING|URI) (medium (COMMA medium)*)? SEMI
    ;

// ---------
// Media.   Introduce a set of rules that are to be used if the consumer indicates
//          it belongs to the signified medium.
// Media can also have a ruleset in them.  
//
media
    : MEDIA_SYM (m+=medium (COMMA m+=medium)*)
        LBRACE
            ((declaration) => b+=declaration SEMI 
            | b+=ruleSet )*
        RBRACE
    -> ^(MEDIA_SYM ^(MEDIUM_DECLARATION $m*) $b*) 
    ; 

// ---------    
// Medium.  The name of a medim that are particulare set of rules applies to.
//
medium
    : IDENT 
    ;
    

bodylist
    : bodyset*
    ;
    
bodyset
    : ruleSet
    | media
    | page
    | fontface
//    | variabledeclaration
    ;   
    
//variabledeclaration
//    : VARIABLE COLON expr SEMI -> ^(VARIABLE_DECLARATION VARIABLE expr)
//    ;
//
//variablereference
//    : '@' VARIABLE -> ^(VARIABLE_REFERENCE VARIABLE)
//    ;

fontface
    : FONT_FACE_SYM^ 
        LBRACE!
            declaration SEMI! (declaration SEMI!)*
        RBRACE!
    ;

page
    : PAGE_SYM pseudoPage?
        LBRACE
            declaration SEMI (declaration SEMI)*
        RBRACE
    ;
    
pseudoPage
    : COLON IDENT
    ;
    
//NOTE: opeq here is just a workaround, but it seems to be sufficient for a less -> css compiler 
operator
    : SOLIDUS
    | COMMA
    | STAR
//    | (PLUS)=>PLUS //TODO:why do I have to do this with plus and minus?
//    | (MINUS)=>MINUS
//    | OPEQ
    | ( -> EMPTY_SEPARATOR) 
    ;
    
combinator
    : PLUS
    | GREATER
    | (EMPTY_COMBINATOR) => EMPTY_COMBINATOR //suppressing warning
    | ( -> EMPTY_COMBINATOR)
    ;
    
unaryOperator
    : MINUS
    | PLUS
    ;  
    
property
    : IDENT
    ;
    
// ruleSet can contain other rulesets.
//TODO: this rule generates warning: Decision can match input such as "IDENT" using multiple alternatives: 1, 2
////the last declaration does not have to have semicolon
ruleSet
    : a+=selector (COMMA a+=selector)*
        LBRACE
            (  (b+=declaration_both_cases ) /*| b+=variabledeclaration*/ 
//               | (b+=combinator b+=ruleSet) 
//               | ('&' COLON b+=ruleSet)
             )* 
        RBRACE
     -> ^(RULESET $a* $b*)
    ;

//That empty combinator in the beginning is a result of HASH_COMBINATOR hack. 
selector
    : EMPTY_COMBINATOR? a+=simpleSelector (a+=combinator a+=simpleSelector)*
    -> ^(SELECTOR ($a)* )
    ;

simpleSelector
    : (a+=elementName 
        ((esPred)=>a+=elementSubsequent)*
        
    | ((esPred)=>a+=elementSubsequent)+
    ) -> ^(SIMPLE_SELECTOR ($a)* )
    ;
    
esPred
    : HASH | DOT | LBRACKET | COLON
    ;
    
elementSubsequent
    : HASH -> ^(ID_SELECTOR HASH)
    | cssClass
    | attrib
    | pseudo
    ;

cssClass
    : DOT IDENT -> ^(CSS_CLASS IDENT)
    ;
    
elementName
    : (IDENT -> ^(ELEMENT_NAME IDENT)) 
    | (STAR -> ^(ELEMENT_NAME STAR))
    ;

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
    : (COLON COLON? 
            a=IDENT
                ( // Function
                
                    (LPAREN ( b=pseudoparameters ) RPAREN) 
                )?
      ) -> ^(PSEUDO $a $b*)
    ;
    
 pseudoparameters:
      (IDENT) => IDENT
    | (NUMBER) => NUMBER
    | simpleSelector 
 ;

//| simpleSelector| NUMBER
//see http://www.w3.org/TR/selectors/#nth-child-pseudo
//nth and other special pseudos are going to be lexer tokens. Their parameters 
//work differently than anything else and correspondig lexer rules and parser 
//would have to be too complicated otherwise.
//nth
//    : (PLUS | MINUS)? (REPEATER | N) ((PLUS | MINUS) NUMBER)?
//    | (PLUS | MINUS)? NUMBER
//    | ODD
//    | EVEN
//    ;

//css does not require ; in last declaration
//declaration can refer also to a mixin - removed for now, I will handle mixins later
//TODO: what does less.js do if the statement before nested rule miss semicolon?
declaration_both_cases
    : declaration ((RBRACE)=>
    | SEMI!);
     
declaration
    : property COLON expr? prio? -> ^(DECLARATION property expr? prio?)
    ;
    
prio
    : IMPORTANT_SYM
    ;
    
expr
    : a=term (b+=operator c+=term)* -> ^(EXPRESSION $a ($b $c)*)
    ;
    
term
    : (a+=value_term
    | a+=function
    | a+=special_function
//    | variablereference
    | a+=hexColor)
    -> ^(TERM $a*)  
    ;
    
value_term
    : unaryOperator?
        (
              NUMBER
            | PERCENTAGE
            | LENGTH
            | EMS
            | EXS
            | ANGLE
            | UNKNOWN_DIMENSION
            | TIME
            | FREQ
//            | VARIABLE
        )
    | STRING
    | IDENT
    ;

special_function
    : URI
    ;

hexColor
    : EMPTY_COMBINATOR? HASH -> HASH
    ;

function
    : a=IDENT LPAREN b=expr RPAREN -> ^(TERM_FUNCTION $a $b*)
    ;
    
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
//      is allowed to be written as in escaped form we basically define each
//      character as a fragment and reuse it in all other rules.
// ==============================================================

fragment  EMPTY_COMBINATOR: ;

// --------------------------------------------------------------
// Define all the fragments of the lexer. These rules neither recognize
// nor create tokens, but must be called from non-fragment rules, which
// do create tokens, using these fragments to either purely define the
// token number, or by calling them to match a certain portion of
// the token string.
//

fragment    HEXCHAR     : ('a'..'f'|'A'..'F'|'0'..'9')  ;

fragment    NONASCII    : '\u0080'..'\uFFFF'            ;   // NB: Upper bound should be \u4177777

fragment    UNICODE     : '\\' HEXCHAR 
                                (HEXCHAR 
                                    (HEXCHAR 
                                        (HEXCHAR 
                                            (HEXCHAR HEXCHAR?)?
                                        )?
                                    )?
                                )? 
                                ('\r'|'\n'|'\t'|'\f'|' ')*  ;
                                
fragment    ESCAPE      : UNICODE | '\\' ~('\r'|'\n'|'\f'|HEXCHAR)  ;

fragment    NMSTART     : '_'
                        | 'a'..'z'
                        | 'A'..'Z'
                        | NONASCII
                        | ESCAPE
                        ;

fragment    NMCHAR      : '_'
                        | 'a'..'z'
                        | 'A'..'Z'
                        | '0'..'9'
                        | '-'
                        | NONASCII
                        | ESCAPE
                        ;
                        
fragment    NAME        : NMCHAR+   ;
fragment    UNKNOWN_DIMENSION        : NMSTART NMCHAR*   ;

// The original URL did not allowed characters, '.', '=', ':', ';', ','  and so on
// TODO: For now, I added only those characters that appear in less.js css test case.
fragment    URL         : ( 
                              '['|'!'|'#'|'$'|'%'|'&'|'*'|'~'|'/'|'.'|'='|':'|';'|','|'\r'|'\n'|'\t'|' '|'+'
                            | NMCHAR
                          )*
                        ;

                        
// Basic Alpha characters in upper, lower and escaped form. Note that   
// whitespace and newlines are unimportant even within keywords. We do not
// however call a further fragment rule to consume these characters for
// reasons of performance - the rules are still eminently readable.
//
fragment    A   :   ('a'|'A') ('\r'|'\n'|'\t'|'\f'|' ')*    
                |   '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'1'
                ;
fragment    B   :   ('b'|'B') ('\r'|'\n'|'\t'|'\f'|' ')*    
                |   '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'2'
                ;
fragment    C   :   ('c'|'C') ('\r'|'\n'|'\t'|'\f'|' ')*    
                |   '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'3'
                ;
fragment    D   :   ('d'|'D') ('\r'|'\n'|'\t'|'\f'|' ')*    
                |   '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'4'
                ;
fragment    E   :   ('e'|'E') ('\r'|'\n'|'\t'|'\f'|' ')*    
                |   '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'5'
                ;
fragment    F   :   ('f'|'F') ('\r'|'\n'|'\t'|'\f'|' ')*    
                |   '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'6'
                ;
fragment    G   :   ('g'|'G') ('\r'|'\n'|'\t'|'\f'|' ')* 
                |   '\\'
                        (
                              'g'
                            | 'G'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'7'
                        )
                ;
fragment    H   :   ('h'|'H') ('\r'|'\n'|'\t'|'\f'|' ')* 
                | '\\' 
                        (
                              'h'
                            | 'H'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'8'
                        )   
                ;
fragment    I   :   ('i'|'I') ('\r'|'\n'|'\t'|'\f'|' ')* 
                | '\\' 
                        (
                              'i'
                            | 'I'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'9'
                        )
                ;
fragment    J   :   ('j'|'J') ('\r'|'\n'|'\t'|'\f'|' ')* 
                | '\\' 
                        (
                              'j'
                            | 'J'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('A'|'a')
                        )   
                ;
fragment    K   :   ('k'|'K') ('\r'|'\n'|'\t'|'\f'|' ')* 
                | '\\' 
                        (
                              'k'
                            | 'K'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('B'|'b')
                        )   
                ;
fragment    L   :   ('l'|'L') ('\r'|'\n'|'\t'|'\f'|' ')* 
                | '\\' 
                        (
                              'l'
                            | 'L'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('C'|'c')
                        )   
                ;
fragment    M   :   ('m'|'M') ('\r'|'\n'|'\t'|'\f'|' ')* 
                | '\\' 
                        (
                              'm'
                            | 'M'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('D'|'d')
                        )   
                ;
fragment    N   :   ('n'|'N') ('\r'|'\n'|'\t'|'\f'|' ')* 
                | '\\' 
                        (
                              'n'
                            | 'N'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('E'|'e')
                        )   
                ;
fragment    O   :   ('o'|'O') ('\r'|'\n'|'\t'|'\f'|' ')* 
                | '\\' 
                        (
                              'o'
                            | 'O'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('F'|'f')
                        )   
                ;
fragment    P   :   ('p'|'P') ('\r'|'\n'|'\t'|'\f'|' ')* 
                | '\\'
                        (
                              'p'
                            | 'P'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('0')
                        )   
                ;
fragment    Q   :   ('q'|'Q') ('\r'|'\n'|'\t'|'\f'|' ')* 
                | '\\' 
                        (
                              'q'
                            | 'Q'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('1')
                        )   
                ;
fragment    R   :   ('r'|'R') ('\r'|'\n'|'\t'|'\f'|' ')* 
                | '\\' 
                        (
                              'r'
                            | 'R'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('2')
                        )   
                ;
fragment    S   :   ('s'|'S') ('\r'|'\n'|'\t'|'\f'|' ')* 
                | '\\' 
                        (
                              's'
                            | 'S'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('3')
                        )   
                ;
fragment    T   :   ('t'|'T') ('\r'|'\n'|'\t'|'\f'|' ')* 
                | '\\' 
                        (
                              't'
                            | 'T'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('4')
                        )   
                ;
fragment    U   :   ('u'|'U') ('\r'|'\n'|'\t'|'\f'|' ')* 
                | '\\' 
                        (
                              'u'
                            | 'U'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('5')
                        )
                ;
fragment    V   :   ('v'|'V') ('\r'|'\n'|'\t'|'\f'|' ')* 
                | '\\' 
                        (     'v'
                            | 'V'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('6')
                        )
                ;
fragment    W   :   ('w'|'W') ('\r'|'\n'|'\t'|'\f'|' ')* 
                | '\\' 
                        (
                              'w'
                            | 'W'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('7')
                        )   
                ;
fragment    X   :   ('x'|'X') ('\r'|'\n'|'\t'|'\f'|' ')* 
                | '\\' 
                        (
                              'x'
                            | 'X'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('8')
                        )
                ;
fragment    Y   :   ('y'|'Y') ('\r'|'\n'|'\t'|'\f'|' ')* 
                | '\\' 
                        (
                              'y'
                            | 'Y'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('9')
                        )
                ;
fragment    Z   :   ('z'|'Z') ('\r'|'\n'|'\t'|'\f'|' ')* 
                | '\\' 
                        (
                              'z'
                            | 'Z'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('A'|'a')
                        )
                ;


// -------------
// Comments.    Comments may not be nested, may be multilined and are delimited
//              like C comments: /* ..... */
//              COMMENTS are hidden from the parser which simplifies the parser 
//              grammar a lot.
//
COMMENT         : '/*' ( options { greedy=false; } : .*) '*/'
    
                    {
                        $channel = 2;   // Comments on channel 2 in case we want to find them
                    }
                ;

COMMENT_LITTLE : '//' ( options { greedy=false; } : .*) NL { $channel = HIDDEN; };

// ---------------------
// HTML comment open.   HTML/XML comments may be placed around style sheets so that they
//                      are hidden from higher scope parsing engines such as HTML parsers.
//                      They comment open is therfore ignored by the CSS parser and we hide
//                      it from the ANLTR parser.
//
CDO             : '<!--'

                    {
                        $channel = 3;   // CDO on channel 3 in case we want it later
                    }
                ;
    
// ---------------------            
// HTML comment close.  HTML/XML comments may be placed around style sheets so that they
//                      are hidden from higher scope parsing engines such as HTML parsers.
//                      They comment close is therfore ignored by the CSS parser and we hide
//                      it from the ANLTR parser.
//
CDC             : '-->'

                    {
                        $channel = 4;   // CDC on channel 4 in case we want it later
                    }
                ;
                
INCLUDES        : '~='      ;
DASHMATCH       : '|='      ;
PREFIXMATCH       : '^='      ;
SUFFIXMATCH       : '$='      ;
SUBSTRINGMATCH       : '*='      ;


GREATER         : '>'       ;
LBRACE          : '{'       ;
RBRACE          : '}'       ;
LBRACKET        : '['       ;
RBRACKET        : ']'       ;
OPEQ            : '='       ;
SEMI            : ';'       ;
COLON           : ':'       ;
SOLIDUS         : '/'       ;
MINUS           : '-'       ;
PLUS            : '+'       ;
STAR            : '*'       ;
LPAREN          : '('       ;
RPAREN          : ')'       ;
COMMA           : ','       ;
DOT             : '.'       ;

// -----------------
// Literal strings. Delimited by either ' or "
// original grammar considered a single isolated letter to be a string too. 
// therefore  h1, h2 > a > p, h3 would be tokenized as IDENT COLON IDENT GREATER STRING GREATER STRING ...
// removed that option
//the original string definition did not supported escaping '\\\\'* 
fragment    INVALID :;
fragment    ESCAPE_SINGLE_QUOTE :  '\\\''; 
fragment    ESCAPE_DOUBLE_QUOTE :  '\\"'; 
STRING          : '\'' ( (ESCAPE_SINGLE_QUOTE) | ~('\n'|'\r'|'\f'|'\'') )* 
                    (
                          '\''
                        | { $type = INVALID; }
                    )
                    
                | '"' ( (ESCAPE_DOUBLE_QUOTE) | ~('\n'|'\r'|'\f'|'"') )*
                    (
                          '"'
                        | { $type = INVALID; }
                    )
                ;

// -------------
// Identifier.  Identifier tokens pick up properties names and values
//
//
IDENT           : '-'? NMSTART NMCHAR*  ;

// -------------
// Reference.   Reference to an element in the body we are styling, such as <XXXX id="reference">
// 
fragment HASH_FRAGMENT : '#' NAME             ; 
HASH            : HASH_FRAGMENT             ; 
//Hopefully, I will not destroy document references too much
HASH_COMBINATOR : a=WS b=HASH_FRAGMENT {
                     $a.setType(EMPTY_COMBINATOR);
                     emit($a);
                     $b.setType(HASH);
                     emit($b);
                   }; 

IMPORT_SYM      : '@' I M P O R T       ;
PAGE_SYM        : '@' P A G E           ;
MEDIA_SYM       : '@' M E D I A         ;
FONT_FACE_SYM   : '@' F O N T MINUS F A C E ;
CHARSET_SYM     : '@charset '           ;

//VARIABLE        : '@' NAME              ;

IMPORTANT_SYM   : '!' (WS|COMMENT)* I M P O R T A N T   ;

// ---------
// Numbers. Numbers can be followed by pre-known units or unknown units
//          as well as '%' it is a precentage. Whitespace cannot be between
//          the numebr and teh unit or percent. Hence we scan any numeric, then
//          if we detect one of the lexical sequences for unit tokens, we change
//          the lexical type dynamically.
//
//          Here we first define the various tokens, then we implement the
//          number parsing rule.
//
fragment    EMS         :;  // 'em'
fragment    EXS         :;  // 'ex'
fragment    LENGTH      :;  // 'px'. 'cm', 'mm', 'in'. 'pt', 'pc'
fragment    ANGLE       :;  // 'deg', 'rad', 'grad'
fragment    TIME        :;  // 'ms', 's'
fragment    FREQ        :;  // 'khz', 'hz'
fragment    REPEATER    :;   // n found in n-th child formulas if I would not do that, the dimension would eat it. TODO: maybe multiple small grammars for different css aspects would be nicer. 
fragment    PERCENTAGE  :;  // '%'

// there is no reason to require a dot inside a number
fragment PURE_NUMBER: '0'..'9' ('.'? '0'..'9'+)?
            | '.' '0'..'9'+;
            
NUMBER
    :   (PURE_NUMBER
              
        )
        (
              (E (M|X))=>
                E
                (
                      M     { $type = EMS;          }
                    | X     { $type = EXS;          }
                )
            | (P(X|T|C))=>
                P
                (
                      X     
                    | T
                    | C
                )
                            { $type = LENGTH;       }   
            | (C M)=>
                C M         { $type = LENGTH;       }
            | (M (M|S))=> 
                M
                (
                      M     { $type = LENGTH;       }
            
                    | S     { $type = TIME;         }
                )
            | (I N)=>
                I N         { $type = LENGTH;       }
            
            | (D E G)=>
                D E G       { $type = ANGLE;        }
            | (R A D)=>
                R A D       { $type = ANGLE;        }
            
            | (S)=>S        { $type = TIME;         }
                
            | (K? H Z)=>
                K? H    Z   { $type = FREQ;         }

            | (N)=>
                N           { $type = REPEATER;         }
            
            | '%'           { $type = PERCENTAGE;   }
            
            | // Just a number
        )
    ;
    
NUMBER_UNKNOWN_DIMENSION: PURE_NUMBER UNKNOWN_DIMENSION  { $type = UNKNOWN_DIMENSION; } ; 

// ------------
// url and uri.
//TODO this gives warning, but we have to handle it differently thanks to variables
URI :   U R L
        '('
            ((WS)=>WS)? (URL|STRING) WS?
        ')'
    ;

// odd and even for nth-child FIXME: this will each a class name ODD too!!!
ODD: O D D;
EVEN: E V E N;

// -------------
// Whitespace.  Though the W3 standard shows a Yacc/Lex style parser and lexer
//              that process the whitespace within the parser, ANTLR does not
//              need to deal with the whitespace directly in the parser.
//
WS      : (' '|'\t')+           { $channel = HIDDEN;    }   ;
fragment NL      : ('\r' '\n'? | '\n');
NEW_LINE: NL   { $channel = HIDDEN;    }   ;

// -------------
//  Illegal.    Any other character shoudl not be allowed.
//
