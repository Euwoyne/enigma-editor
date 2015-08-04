/*
 * Copyright 2008 Ayman Al-Sairafi ayman.alsairafi@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License
 *       at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on "lua.flex" from the package "jsyntaxpane-0.9.5-b29" by Ayman Al-Sairafi
 *
 * Modifications by Dominik Lehmann:
 *   Modified 01/02/2015: Added Lua5 standard library functions and types
 */

package jsyntaxpane.lexers;

import jsyntaxpane.Token;
import jsyntaxpane.TokenType;

%%

%public
%class Lua5Lexer
%extends DefaultJFlexLexer
%final
%unicode
%char
%type Token

%{
    public Lua5Lexer() {
        super();
    }

    @Override
    public int yychar() {
        return yychar;
    }

    private static final byte PAREN       = 1;
    private static final byte BRACKET     = 2;
    private static final byte BRACE       = 3;
    private static final byte ENDBLOCK    = 4;
    private static final byte REPEATBLOCK = 5;

	TokenType commentType;
    int       startLength;
%}

/* main character classes */
LineTerminator = \r|\n|\r\n

WhiteSpace = {LineTerminator} | [ \t\f]+

CommentStart = \[=*\[
CommentEnd   = \]=*\]

/* identifiers */
Identifier = [:jletter:][:jletterdigit:]*

/* integer literals */
DecIntegerLiteral = [0-9]+
HexDigit          = [0-9a-fA-F]

HexIntegerLiteral = 0x{HexDigit}+

/* floating point literals */        
DoubleLiteral = ({FLit1}|{FLit2}) {Exponent}?

FLit1    = [0-9]+(\.[0-9]*)?
FLit2    = \.[0-9]+ 
Exponent = [eE] [+-]? [0-9]+

/* string and character literals */
StringCharacter1 = [^\r\n\'\\]
StringCharacter2 = [^\r\n\"\\]

/* label */
Label = ::{Identifier}::

%state STRING1
%state STRING2
%state BLOCKCOMMENT

%state COMMENT
%state LINECOMMENT

%%

<YYINITIAL>
{
    /* keywords */
    "break"          |
    "else"           |
    "elseif"         |
    "for"            |
    "if"             |
    "local"          |
    "return"         |
    "while"          {return token(TokenType.KEYWORD);}
    
    "function"       |
    "then"           |
    "do"             {return token(TokenType.KEYWORD,  ENDBLOCK);}
    "end"            {return token(TokenType.KEYWORD, -ENDBLOCK);}
    
    "repeat"         {return token(TokenType.KEYWORD,  REPEATBLOCK);}
    "until"          {return token(TokenType.KEYWORD, -REPEATBLOCK);}
    
    "and"            |
    "in"             |
    "not"            |
    "or"             {return token(TokenType.KEYWORD);}
    
    /* functions */
    "assert"         |
    "collectgarbage" |
    "dofile"         |
    "error"          |
    "getfenv"        |
    "getmetatable"   |
    "gcinfo"         |
    "ipairs"         |
    "load"           |
    "loadfile"       |
    "loadlib"        |
    "loadstring"     |
    "next"           |
    "pairs"          |
    "pcall"          |
    "print"          |
    "rawequal"       |
    "rawget"         |
    "rawset"         |
    "require"        |
    "select"         |
    "setfenv"        |
    "setmetatable"   |
    "tonumber"       |
    "tostring"       |
    "type"           |
    "unpack"         |
    "xpcall"         {return token(TokenType.KEYWORD2);}
    
    /* literals */
    "true"           |
    "false"          |
    "nil"            {return token(TokenType.TYPE);}
    
    /* standard library types */
    "string"         |
    "table"          |
    "math"           |
    "io"             |
    "os"             |
    "debug"          {return token(TokenType.TYPE2);}
    
    /* package control */
    "coroutine"      |
    "module"         |
    "require"        |
    "package"        {return token(TokenType.TYPE3);}
    
    /* operators */
    "+"   |
    "-"   |
    "*"   |
    "/"   |
    "%"   |
    "^"   |
    "#"   |
    "&"   |
    "~"   |
    "|"   |
    "<<"  |
    ">>"  |
    "//"  |
    "=="  |
    "~="  |
    "<="  |
    ">="  |
    "<"   |
    ">"   |
    "="   |
    ";"   |
    ":"   |
    ","   |
    "."   |
    ".."  |
    "..." {return token(TokenType.OPERATOR);} 
    
    /* parentheses */
    "("   {return token(TokenType.OPERATOR,  PAREN);}
    ")"   {return token(TokenType.OPERATOR, -PAREN);}
    "{"   {return token(TokenType.OPERATOR,  BRACE);}
    "}"   {return token(TokenType.OPERATOR, -BRACE);}
    "["   {return token(TokenType.OPERATOR,  BRACKET);}
    "]"   {return token(TokenType.OPERATOR, -BRACKET);}
    
    /* labels */
    {Label} {return token(TokenType.OPERATOR);}
    
    /* comments */
    "--"            {
                        yybegin(COMMENT);
                        tokenStart  = yychar;
                        tokenLength = yylength();
                    }
    
    {CommentStart}  {
                        yybegin(BLOCKCOMMENT);
                        commentType = TokenType.COMMENT2;
                        tokenStart  = yychar;
                        tokenLength = yylength();
                        startLength = tokenLength;
                    }
    
    /* string literal */
    \'              {
                        yybegin(STRING1);
                        tokenStart = yychar;
                        tokenLength = 1;
                    }
    
    \"              {  
                        yybegin(STRING2);
                        tokenStart = yychar; 
                        tokenLength = 1; 
                    }
    
    /* numeric literals */
    {DecIntegerLiteral}     |
    {HexIntegerLiteral}     |
    {DoubleLiteral}         {return token(TokenType.NUMBER);}
    
    /* whitespace */
    {WhiteSpace}            {}
    
    /* identifiers */ 
    {Identifier}            {return token(TokenType.IDENTIFIER);}
}

/* general comment state */
<COMMENT>
{
    {CommentStart}      {
                            yybegin(BLOCKCOMMENT);
                            commentType = TokenType.COMMENT2;
                            tokenLength += yylength();
                            startLength = yylength();
                        }
    
    {LineTerminator}    {
                            yybegin(YYINITIAL);
                            return token(TokenType.COMMENT, tokenStart, tokenLength);
                        }
    
    .                   {
                            yybegin(LINECOMMENT);
                            tokenLength += yylength();
                        }
    
    <<EOF>>             {
                            yybegin(YYINITIAL);
                            return token(TokenType.COMMENT, tokenStart, tokenLength);
                        }
}

/* block comment state */
<BLOCKCOMMENT>
{
    {CommentEnd}        {
                            if (startLength == yylength())
                            {
                                tokenLength += yylength();
                                yybegin(YYINITIAL);
                                return token(commentType, tokenStart, tokenLength);
                            }
                            else
                            {
                                tokenLength++;
                                yypushback(yylength() - 1);
                            }
                        }
    
    {LineTerminator}    {tokenLength += yylength();}	                             
    .                   {++tokenLength;}
    
    <<EOF>>             {
                            yybegin(YYINITIAL);
                            return token(commentType, tokenStart, tokenLength);
                        }
}

/* line comment state */
<LINECOMMENT>
{
    {LineTerminator}    {
                            yybegin(YYINITIAL);
                            tokenLength += yylength();
                            return token(TokenType.COMMENT, tokenStart, tokenLength);
                        }
    
    {LineTerminator}    {tokenLength += yylength();}
    .                   {tokenLength++;}
    
    <<EOF>>             {
                            yybegin(YYINITIAL);
                            return token(TokenType.COMMENT, tokenStart, tokenLength);
                        }
}

/* string (double quotes) */
<STRING2>
{
    \"                  {
                            yybegin(YYINITIAL); 
                            return token(TokenType.STRING, tokenStart, tokenLength + 1);
                        }
    
    {StringCharacter2}+ {tokenLength += yylength();}
    \\.                 {tokenLength += 2;}
    {LineTerminator}    {yybegin(YYINITIAL);}
    
    <<EOF>>	            {
                            yybegin(YYINITIAL);
                            return token(TokenType.STRING, tokenStart, tokenLength);
                        }
}

/* string (single quotes) */
<STRING1>
{
    \'                  {
                            yybegin(YYINITIAL);
                            return token(TokenType.STRING, tokenStart, tokenLength + 1);
                        }
    
    {StringCharacter1}+ {tokenLength += yylength();}
    \\.                 {tokenLength += 2;}
    {LineTerminator}    {yybegin(YYINITIAL);}
    
    <<EOF>>             {
                            yybegin(YYINITIAL);
                            return token(TokenType.STRING, tokenStart, tokenLength);
                        }
}

/* error fallback */
[^]      {}
<<EOF>>  {return null;}

