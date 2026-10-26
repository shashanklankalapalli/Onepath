import React, { createContext, useContext, useState, useEffect } from 'react';
import { User, AuthResponse } from '../types';
import { authService } from '../services/api';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: { email: string; password: string }) => Promise<void>;
  register: (data: { name: string; email: string; password: string; role?: string; assignedCollegeId?: number }) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('onepath_token'));
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const fetchUser = async () => {
      if (token) {
        try {
          const userData = await authService.getCurrentUser();
          setUser(userData);
        } catch (err) {
          console.error('Failed to fetch current user session', err);
          localStorage.removeItem('onepath_token');
          setToken(null);
          setUser(null);
        }
      }
      setIsLoading(false);
    };
    fetchUser();
  }, [token]);

  const login = async (credentials: { email: string; password: string }) => {
    const res: AuthResponse = await authService.login(credentials);
    localStorage.setItem('onepath_token', res.token);
    setToken(res.token);
    setUser({
      id: res.id,
      name: res.name,
      email: res.email,
      role: res.role,
      assignedCollegeId: res.assignedCollegeId,
    });
  };

  const register = async (data: { name: string; email: string; password: string; role?: string; assignedCollegeId?: number }) => {
    const res: AuthResponse = await authService.register(data);
    localStorage.setItem('onepath_token', res.token);
    setToken(res.token);
    setUser({
      id: res.id,
      name: res.name,
      email: res.email,
      role: res.role,
      assignedCollegeId: res.assignedCollegeId,
    });
  };

  const logout = () => {
    localStorage.removeItem('onepath_token');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!user,
        isLoading,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
