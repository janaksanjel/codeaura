// TypeScript service demo
// handles all user-related API calls
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { tap } from 'rxjs/operators';
import { catchError } from 'rxjs/operators';

/* User interface */
interface User {
  id: number;       // user id
  name: string;     // full name
  email: string;    // email address
}

// Injectable decorator marks this as a service
@Injectable({ providedIn: 'root' })
export class UserService{
  // base URL for API
  private baseUrl = 'https://api.example.com';

  constructor(private http: HttpClient){} // inject HttpClient

  // get all users
  getUsers(): Observable<User[]>{
    // make GET request
    return this.http.get<User[]>(`${this.baseUrl}/users`).pipe(
      tap(data => console.log(data)), // log response
      map(users => users.filter(u => u.id > 0)), // filter valid
      catchError(err =>{
        console.error(err); // handle error
        throw err;
      })
    );
  }



  // get single user by id
  getUserById(id: number): Observable<User>{
    return this.http.get<User>(`${this.baseUrl}/users/${id}`);
  }
}
