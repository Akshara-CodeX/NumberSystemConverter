# Number System Converter (Java)

A menu-driven Java program that converts numbers between **Decimal, Binary, Octal and Hexadecimal**, with support for **negative numbers, fractions and 1's / 2's complement**.

## Features

- 12 conversions between Decimal (base 10), Binary (base 2), Octal (base 8) and Hexadecimal (base 16)
- Accepts **negative numbers** (e.g. `-45`) and **fractional numbers** (e.g. `10.101`)
- Binary logic for negative numbers: **1's complement** and **2's complement** (with steps shown)
- Written **manually** (no `Integer.toBinaryString()` etc.) so the logic is easy to learn from
- Input validation (wrong digits, bad choices, out-of-range values)
- Runs in a loop until the user chooses to exit

## Menu

```
 1. Decimal     -> Binary           10. Hexadecimal -> Decimal
 2. Decimal     -> Octal            11. Hexadecimal -> Binary
 3. Decimal     -> Hexadecimal      12. Hexadecimal -> Octal
 4. Binary      -> Decimal
 5. Binary      -> Octal            13. Decimal -> Binary (2's complement)
 6. Binary      -> Hexadecimal      14. Binary (2's complement) -> Decimal
 7. Octal       -> Decimal          15. Binary -> 1's and 2's complement
 8. Octal       -> Binary
 9. Octal       -> Hexadecimal
```

## How to run

Requires Java (JDK 11 or later).

```bash
javac NumberSystemConverter.java
java NumberSystemConverter
```

## Sample output

```
>> Decimal -45.625  =  Binary -101101.101
>> Binary -10.101  =  Decimal -2.625
>> Binary -1111.1  =  Hexadecimal -F.8

   Step 1: binary of 45      = 00101101
   Step 2: 1's complement    = 11010010
   Step 3: add 1 (2's comp.) = 11010011
>> -45 in 8-bit 2's complement = 11010011
>> 11010011 (8 bits) = -45 in decimal
```

## How it works

| Function | Purpose |
|---|---|
| `decimalToBase()` | Whole number -> any base by repeated division (read remainders in reverse) |
| `baseToDecimal()` | Whole number of any base -> decimal using place values |
| `fractionToDecimal()` | Digits after the point -> exact decimal fraction |
| `fractionFromDecimal()` | Decimal fraction -> any base by repeated multiplication |
| `convert()` | Joins sign + whole part + fraction part |
| `onesComplement()` / `twosComplement()` | Flip all bits / flip all bits and add 1 |
| `isValidNumber()` | Checks digits are legal for the chosen base |

Each of the 12 menu conversions has its own function (e.g. `binaryToHexadecimal()`) built on these core functions.

## Limits

- Maximum digits before the point: 18 (decimal), 62 (binary), 20 (octal), 15 (hexadecimal)
- Fractions that never end (e.g. decimal `0.1` in binary) are cut after 10 digits, and the program tells you
- 2's complement works with 8, 16 or 32 bits

## Project structure

```
NumberSystemConverter.java   # the complete program
README.md
```

🙋 Author

Built by Akshara as a personal project.

Feedback and suggestions are always welcome — feel free to open an issue or connect!

📄 License

This project is open for learning purposes. Feel free to fork and build on it.