import React, { useState, useEffect, useRef } from 'react';
import { useLocation, Link } from 'react-router-dom';
import {
  Sparkles,
  MessageSquare,
  X,
  Send,
  RotateCcw,
  Bot,
  User as UserIcon,
  Loader2,
  ExternalLink,
  ChevronDown,
  Info,
  ShieldCheck,
  Zap,
} from 'lucide-react';
import { chatbotApi } from '../services/api';
import { ChatMessage } from '../types';

const STORAGE_KEY = 'onepath_chat_session_v1';

const INITIAL_WELCOME_MESSAGE: ChatMessage = {
  id: 'welcome',
  role: 'assistant',
  content: `Hello! 👋 I am your **OnePath AI Assistant**.

I can help you navigate official procedures, calculate exact fees, check eligibility, and understand required documents for:
* **Passport Application & Renewal** (Fresh, Tatkaal, Student proofs)
* **Driving Licence & Vehicle Registration** (LL, Permanent DL, RC)
* **EWS, Family Income & Caste Certificates** (DoPT Central & State e-District)
* **College & University Administrative Processes**

How can I help you today?`,
  timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
  suggestedActions: [
    'How do I apply for a Passport?',
    'What are the fees for a Driving Licence?',
    'Who is eligible for an EWS certificate?',
    'What documents do I need as a student?',
  ],
};

export const ChatbotWidget: React.FC = () => {
  const location = useLocation();
  const [isOpen, setIsOpen] = useState<boolean>(false);
  const [hasUnread, setHasUnread] = useState<boolean>(false);
  const [inputMessage, setInputMessage] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [messages, setMessages] = useState<ChatMessage[]>(() => {
    try {
      const saved = sessionStorage.getItem(STORAGE_KEY);
      if (saved) {
        const parsed = JSON.parse(saved);
        if (Array.isArray(parsed) && parsed.length > 0) {
          return parsed;
        }
      }
    } catch (e) {
      console.error('Failed to restore chat session:', e);
    }
    return [INITIAL_WELCOME_MESSAGE];
  });

  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLTextAreaElement>(null);

  // Save conversation to sessionStorage to persist across route navigation
  useEffect(() => {
    try {
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(messages));
    } catch (e) {
      console.error('Failed to save chat session:', e);
    }
  }, [messages]);

  // Scroll to bottom on new message
  useEffect(() => {
    if (isOpen) {
      messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages, isOpen, isLoading]);

  // Focus input on open
  useEffect(() => {
    if (isOpen) {
      setHasUnread(false);
      setTimeout(() => inputRef.current?.focus(), 150);
    }
  }, [isOpen]);

  const handleSendMessage = async (textToSend?: string) => {
    const text = (textToSend || inputMessage).trim();
    if (!text || isLoading) return;

    const userMsg: ChatMessage = {
      id: 'usr_' + Date.now(),
      role: 'user',
      content: text,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };

    const updatedMessages = [...messages, userMsg];
    setMessages(updatedMessages);
    setInputMessage('');
    setIsLoading(true);

    try {
      // Build conversation history (excluding initial greeting to save tokens)
      const history = updatedMessages
        .filter((m) => m.id !== 'welcome' && !m.isError)
        .slice(-8)
        .map((m) => ({
          role: m.role,
          content: m.content,
        }));

      const res = await chatbotApi.sendMessage({
        message: text,
        conversationHistory: history,
        currentPage: location.pathname,
      });

      const assistantMsg: ChatMessage = {
        id: 'ast_' + Date.now(),
        role: 'assistant',
        content: res.reply,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        suggestedActions: res.suggestedActions,
        isError: res.error,
      };

      setMessages((prev) => [...prev, assistantMsg]);
    } catch (err: any) {
      const errorMsg: ChatMessage = {
        id: 'err_' + Date.now(),
        role: 'assistant',
        content:
          err.response?.data?.message ||
          'Sorry, I encountered a temporary network issue connecting to the OnePath assistant. Please try again.',
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        isError: true,
      };
      setMessages((prev) => [...prev, errorMsg]);
    } finally {
      setIsLoading(false);
      if (!isOpen) {
        setHasUnread(true);
      }
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  const handleClearChat = () => {
    if (window.confirm('Start a new conversation? This will clear the current chat history.')) {
      setMessages([INITIAL_WELCOME_MESSAGE]);
      sessionStorage.removeItem(STORAGE_KEY);
    }
  };

  // Helper to format basic markdown (bold, lists, links, headers)
  const renderFormattedContent = (content: string) => {
    const lines = content.split('\n');
    return (
      <div className="space-y-1.5 text-xs sm:text-[13px] leading-relaxed">
        {lines.map((line, idx) => {
          const trimmed = line.trim();
          if (!trimmed) {
            return <div key={idx} className="h-1" />;
          }

          // Headers (### or ##)
          if (trimmed.startsWith('### ')) {
            return (
              <h4 key={idx} className="font-bold text-slate-900 text-sm mt-2 mb-0.5">
                {formatInline(trimmed.replace('### ', ''))}
              </h4>
            );
          }
          if (trimmed.startsWith('## ')) {
            return (
              <h3 key={idx} className="font-extrabold text-slate-950 text-sm mt-2 mb-1">
                {formatInline(trimmed.replace('## ', ''))}
              </h3>
            );
          }

          // Blockquote (> Notice:)
          if (trimmed.startsWith('> ')) {
            return (
              <div
                key={idx}
                className="bg-amber-50/80 border-l-2 border-amber-500 px-2.5 py-1.5 rounded-r-lg text-amber-900 text-xs my-1"
              >
                {formatInline(trimmed.replace('> ', ''))}
              </div>
            );
          }

          // Unordered Bullet list (* or -)
          if (trimmed.startsWith('* ') || trimmed.startsWith('- ')) {
            return (
              <div key={idx} className="flex items-start gap-1.5 pl-1">
                <span className="text-brand-500 font-bold mt-0.5">•</span>
                <span className="flex-1">{formatInline(trimmed.substring(2))}</span>
              </div>
            );
          }

          // Numbered list (1. 2.)
          const numMatch = trimmed.match(/^(\d+)\.\s+(.*)/);
          if (numMatch) {
            return (
              <div key={idx} className="flex items-start gap-1.5 pl-1">
                <span className="text-brand-600 font-bold text-xs">{numMatch[1]}.</span>
                <span className="flex-1">{formatInline(numMatch[2])}</span>
              </div>
            );
          }

          return <p key={idx}>{formatInline(line)}</p>;
        })}
      </div>
    );
  };

  // Helper for inline bold, links, code
  const formatInline = (text: string) => {
    // Process markdown links [text](url)
    const linkRegex = /\[(.*?)\]\((.*?)\)/g;
    const parts: (string | React.ReactNode)[] = [];
    let lastIndex = 0;
    let match;

    while ((match = linkRegex.exec(text)) !== null) {
      if (match.index > lastIndex) {
        parts.push(formatBold(text.substring(lastIndex, match.index)));
      }
      const label = match[1];
      const url = match[2];

      if (url.startsWith('/')) {
        parts.push(
          <Link
            key={match.index}
            to={url}
            className="text-brand-600 font-bold hover:underline inline-flex items-center gap-0.5"
            onClick={() => setIsOpen(false)}
          >
            {label}
          </Link>
        );
      } else {
        parts.push(
          <a
            key={match.index}
            href={url}
            target="_blank"
            rel="noopener noreferrer"
            className="text-brand-600 font-bold hover:underline inline-flex items-center gap-0.5"
          >
            {label}
            <ExternalLink className="w-3 h-3 inline" />
          </a>
        );
      }
      lastIndex = linkRegex.lastIndex;
    }

    if (lastIndex < text.length) {
      parts.push(formatBold(text.substring(lastIndex)));
    }

    return parts;
  };

  const formatBold = (text: string) => {
    const boldRegex = /\*\*(.*?)\*\*/g;
    const parts: (string | React.ReactNode)[] = [];
    let lastIndex = 0;
    let match;

    while ((match = boldRegex.exec(text)) !== null) {
      if (match.index > lastIndex) {
        parts.push(text.substring(lastIndex, match.index));
      }
      parts.push(
        <strong key={match.index} className="font-bold text-slate-900">
          {match[1]}
        </strong>
      );
      lastIndex = boldRegex.lastIndex;
    }

    if (lastIndex < text.length) {
      parts.push(text.substring(lastIndex));
    }

    return parts;
  };

  return (
    <>
      {/* 1. FLOATING CHAT BUTTON */}
      <div className="fixed bottom-5 right-5 sm:bottom-6 sm:right-6 z-40">
        <button
          id="onepath-chat-button"
          onClick={() => setIsOpen((prev) => !prev)}
          className={`relative flex items-center justify-center p-3.5 sm:p-4 rounded-full shadow-xl transition-all duration-300 transform active:scale-95 ${
            isOpen
              ? 'bg-slate-900 text-white hover:bg-slate-800 rotate-90'
              : 'bg-gradient-to-tr from-brand-600 via-brand-700 to-indigo-600 text-white hover:shadow-brand-500/30 hover:scale-105'
          }`}
          aria-label={isOpen ? 'Close OnePath AI Assistant' : 'Open OnePath AI Assistant'}
        >
          {isOpen ? (
            <X className="w-6 h-6" />
          ) : (
            <>
              <Sparkles className="w-6 h-6 animate-pulse" />
              {hasUnread && (
                <span className="absolute -top-1 -right-1 w-3.5 h-3.5 bg-emerald-500 rounded-full border-2 border-white animate-ping" />
              )}
            </>
          )}
        </button>
      </div>

      {/* 2. CHAT PANEL MODAL / DRAWER */}
      {isOpen && (
        <div
          id="onepath-chat-panel"
          className="fixed bottom-20 right-3 sm:right-6 z-50 w-[calc(100vw-1.5rem)] sm:w-[420px] max-w-[95vw] h-[580px] max-h-[82vh] bg-white rounded-3xl border border-slate-200 shadow-2xl flex flex-col overflow-hidden animate-in fade-in slide-in-from-bottom-4 duration-200"
        >
          {/* Header */}
          <div className="bg-gradient-to-r from-slate-900 via-brand-950 to-slate-900 text-white px-4 sm:px-5 py-3.5 flex items-center justify-between shadow-sm flex-shrink-0">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-xl bg-gradient-to-tr from-brand-500 to-indigo-500 flex items-center justify-center shadow-inner text-white">
                <Bot className="w-4 h-4" />
              </div>
              <div>
                <div className="flex items-center gap-1.5">
                  <h3 className="text-sm font-bold tracking-tight text-white">OnePath AI Assistant</h3>
                  <span className="inline-block w-2 h-2 rounded-full bg-emerald-400" />
                </div>
                <p className="text-[10px] text-slate-300 font-medium">Official Guidelines & Portal Guidance</p>
              </div>
            </div>

            <div className="flex items-center gap-1 text-slate-300">
              <button
                onClick={handleClearChat}
                title="Start new conversation"
                className="p-1.5 hover:text-white hover:bg-white/10 rounded-lg transition"
                aria-label="Clear Chat"
              >
                <RotateCcw className="w-3.5 h-3.5" />
              </button>
              <button
                onClick={() => setIsOpen(false)}
                title="Minimize chat"
                className="p-1.5 hover:text-white hover:bg-white/10 rounded-lg transition"
                aria-label="Minimize Chat"
              >
                <ChevronDown className="w-4 h-4" />
              </button>
            </div>
          </div>

          {/* Subheader Banner */}
          <div className="bg-slate-50 border-b border-slate-100 px-4 py-1.5 flex items-center justify-between text-[11px] text-slate-500 flex-shrink-0">
            <div className="flex items-center gap-1">
              <ShieldCheck className="w-3.5 h-3.5 text-emerald-600" />
              <span>Grounded in verified government & institutional rules</span>
            </div>
          </div>

          {/* Messages Container */}
          <div className="flex-1 overflow-y-auto p-4 space-y-3.5 bg-slate-50/40">
            {messages.map((msg, index) => {
              const isUser = msg.role === 'user';
              return (
                <div
                  key={msg.id || index}
                  className={`flex flex-col ${isUser ? 'items-end' : 'items-start'} space-y-1`}
                >
                  <div className="flex items-end gap-1.5 max-w-[88%]">
                    {!isUser && (
                      <div className="w-6 h-6 rounded-full bg-brand-50 border border-brand-100 text-brand-700 flex items-center justify-center flex-shrink-0 mb-1">
                        <Bot className="w-3 h-3" />
                      </div>
                    )}

                    <div
                      className={`px-3.5 py-2.5 rounded-2xl text-xs sm:text-[13px] shadow-sm leading-relaxed ${
                        isUser
                          ? 'bg-brand-600 text-white rounded-br-xs'
                          : msg.isError
                          ? 'bg-rose-50 text-rose-900 border border-rose-200 rounded-bl-xs'
                          : 'bg-white text-slate-800 border border-slate-200/80 rounded-bl-xs'
                      }`}
                    >
                      {isUser ? <p className="whitespace-pre-wrap">{msg.content}</p> : renderFormattedContent(msg.content)}
                    </div>
                  </div>

                  {/* Timestamp */}
                  <span className="text-[10px] text-slate-400 px-8">
                    {msg.timestamp}
                  </span>

                  {/* Suggestion Chips */}
                  {!isUser && msg.suggestedActions && msg.suggestedActions.length > 0 && index === messages.length - 1 && (
                    <div className="flex flex-wrap gap-1.5 pt-1.5 pl-7">
                      {msg.suggestedActions.map((action, aIdx) => (
                        <button
                          key={aIdx}
                          onClick={() => handleSendMessage(action)}
                          className="text-[11px] font-medium bg-white hover:bg-brand-50 hover:text-brand-700 text-slate-700 border border-slate-200 hover:border-brand-300 px-2.5 py-1 rounded-full transition shadow-xs text-left"
                        >
                          {action}
                        </button>
                      ))}
                    </div>
                  )}
                </div>
              );
            })}

            {/* Loading Indicator */}
            {isLoading && (
              <div className="flex items-end gap-1.5 max-w-[85%]">
                <div className="w-6 h-6 rounded-full bg-brand-50 border border-brand-100 text-brand-700 flex items-center justify-center flex-shrink-0 mb-1">
                  <Bot className="w-3 h-3" />
                </div>
                <div className="bg-white border border-slate-200/80 rounded-2xl rounded-bl-xs px-3.5 py-2.5 shadow-sm flex items-center gap-1.5 text-slate-500">
                  <span className="text-xs font-medium">OnePath is analyzing</span>
                  <span className="inline-flex gap-1 items-center ml-1">
                    <span className="w-1.5 h-1.5 rounded-full bg-brand-500 animate-bounce" style={{ animationDelay: '0ms' }} />
                    <span className="w-1.5 h-1.5 rounded-full bg-brand-500 animate-bounce" style={{ animationDelay: '150ms' }} />
                    <span className="w-1.5 h-1.5 rounded-full bg-brand-500 animate-bounce" style={{ animationDelay: '300ms' }} />
                  </span>
                </div>
              </div>
            )}

            <div ref={messagesEndRef} />
          </div>

          {/* Input Bar */}
          <div className="p-3 bg-white border-t border-slate-100 flex-shrink-0">
            <form
              onSubmit={(e) => {
                e.preventDefault();
                handleSendMessage();
              }}
              className="flex items-end gap-2 bg-slate-50 border border-slate-200 focus-within:border-brand-500 focus-within:ring-2 focus-within:ring-brand-500/20 rounded-2xl p-1.5 transition"
            >
              <textarea
                ref={inputRef}
                value={inputMessage}
                onChange={(e) => setInputMessage(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="Ask about Passport, Driving Licence, EWS, College..."
                disabled={isLoading}
                rows={1}
                className="flex-1 bg-transparent border-0 resize-none text-xs sm:text-sm px-2.5 py-1 text-slate-900 placeholder:text-slate-400 focus:outline-none max-h-24 disabled:opacity-50"
                style={{ minHeight: '32px' }}
              />
              <button
                type="submit"
                disabled={!inputMessage.trim() || isLoading}
                className="p-2 rounded-xl bg-brand-600 hover:bg-brand-700 active:bg-brand-800 disabled:bg-slate-200 text-white disabled:text-slate-400 transition shadow-xs flex-shrink-0"
                aria-label="Send message"
              >
                {isLoading ? <Loader2 className="w-4 h-4 animate-spin" /> : <Send className="w-4 h-4" />}
              </button>
            </form>
            <p className="text-[10px] text-slate-400 text-center mt-1.5">
              Press Enter to send, Shift+Enter for new line
            </p>
          </div>
        </div>
      )}
    </>
  );
};
