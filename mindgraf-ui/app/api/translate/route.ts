import { GoogleGenerativeAI } from "@google/generative-ai";
import { NextResponse } from 'next/server';

const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY!);

export async function POST(req: Request) {
  try {
    const { englishText } = await req.json();
    const model = genAI.getGenerativeModel({ model: "gemini-2.5-flash" });

    // The System Instruction: Defining the rules of the Mind
   const systemPrompt = `
      You are the MindGRAF Translation Engine for a SNePS-based semantic network.
      Your only job is to translate Natural Language into CLI commands.
      
      LEGAL COMMANDS: [add-to-context, get-attitudes, define-context, set-mode-1]
      
      STRICT SYNTAX RULES:
      1. ONLY output the command string. No conversational text or markdown formatting.
      2. The 'add-to-context' command ONLY takes the formula/relation as an argument separated by a space.
      3. DO NOT include context names (like cDefault) or attitudes (like aBelief) in the add-to-context string.
      
      EXAMPLES:
      Input: "I believe Mary and Dina are sisters"
      Output: add-to-context sister(Mary, Dina)
      
      Input: "Dina is a student"
      Output: add-to-context student(Dina)
      
      Input: "I am certain that Dina is a student"
      Output: add-to-context student(Dina)
    `;

    const result = await model.generateContent([systemPrompt, englishText]);
    const command = result.response.text().trim();

    return NextResponse.json({ command });

  } catch (error) {
    // ADD THIS LINE TO SEE THE REAL ERROR
    console.error("--- GEMINI API CRASH ---", error); 
    
    return NextResponse.json({ error: 'Translation failed' }, { status: 500 });
  }
}